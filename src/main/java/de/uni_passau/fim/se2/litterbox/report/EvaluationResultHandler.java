/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
import de.uni_passau.fim.se2.litterbox.analytics.questions.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.PrintWriterOutput;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles evaluation of student answers and produces result reports.
 */
public class EvaluationResultHandler implements QuestionResultHandler {

    private static final Logger log = Logger.getLogger(EvaluationResultHandler.class.getName());
    private static final TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

    private static final String PATH_SCRATCHBLOCKS_JS = "/js/scratchblocks.min.js";
    private static final String PATH_TRANSLATIONS_JS = "/js/translations-all.js";
    private static final String LOGO_PATH = "/litterbox.svg";
    private static final String FAVICON_PATH = "/favicon.ico";

    private IssueTranslator translator;
    private final Path output;
    private final Path answersFile;
    private final boolean llmEval;
    private final JsonMapper objectMapper;

    // Self-contained evaluation: all data comes from the export
    private List<ExportedAnswer> exportedAnswers;
    private String projectFileName;
    private final List<EvaluatedProject> evaluatedProjects = new ArrayList<>();
    private LLMAnswerEvaluator llmEvaluator;

    public EvaluationResultHandler(IssueTranslator translator, Path output, Path answersFile, boolean llmEval) {
        this.translator = translator;
        this.output = output;
        this.answersFile = answersFile;
        this.llmEval = llmEval;
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    @Override
    public void begin() throws IOException {
        // Load answers from JSON file
        loadAnswers();
    }

    private void loadAnswers() throws IOException {
        AnswersInput input = objectMapper.readValue(answersFile.toFile(), AnswersInput.class);

        projectFileName = input.projectFile();
        exportedAnswers = new ArrayList<>();

        if (input.answers() != null) {
            for (AnswerInputEntry entry : input.answers()) {
                exportedAnswers.add(new ExportedAnswer(
                        entry.questionId(),
                        entry.finder(),
                        entry.sprite(),
                        entry.questionType(),
                        entry.questionText(),
                        entry.dimension(),
                        entry.scope(),
                        entry.scratchBlocks(),
                        entry.choices(),
                        entry.getEffectiveCorrectAnswers(),
                        entry.getEffectiveStudentAnswer()
                ));
            }
        }

        if (input.language() != null && !input.language().isEmpty()) {
            this.translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(input.language()));
            log.info("Using language from answer file: " + input.language());
        }

        // Initialize LLM evaluator if requested
        if (llmEval) {
            String lang = input.language() != null ? input.language() : translator.getLanguage();
            try {
                this.llmEvaluator = new LLMAnswerEvaluator(lang);
                log.info("LLM answer evaluator initialized for language: " + lang);
            } catch (Exception e) {
                log.warning("Failed to initialize LLM evaluator: " + e.getMessage());
            }
        }

        log.info("Loaded " + exportedAnswers.size() + " answers from " + answersFile);
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        // With self-contained exports, we ignore the regenerated questions
        // and evaluate directly from the exported data
        String thisProjectName = projectFile.getFileName().toString();
        if (projectFileName != null && !thisProjectName.equals(projectFileName)) {
            log.warning("Project name mismatch: expected " + projectFileName + " but got " + thisProjectName);
        }

        // Create an AnswerChecker for type-specific evaluation and optional LLM support
        AnswerChecker answerChecker = new AnswerChecker(llmEvaluator, 0.001, translator);

        // Evaluate each exported answer directly
        List<EvaluatedQuestion> evaluatedQuestions = new ArrayList<>();

        for (ExportedAnswer exported : exportedAnswers) {
            evaluatedQuestions.add(evaluateAndCreateQuestion(exported, answerChecker));
        }

        // Create a simple check result using the factory method
        List<AnswerResult> answerResults = evaluatedQuestions.stream()
                .map(eq -> AnswerResult.withFeedback(
                        eq.id(),
                        eq.questionType(),
                        eq.correct(),
                        eq.providedAnswer(),
                        eq.expectedAnswer(),
                        eq.feedback()
                ))
                .toList();

        // Build dimension maps for the check result
        Map<Integer, ProgramDimension> questionDimensions = new HashMap<>();
        Map<Integer, ScopeOfFocus> questionScopes = new HashMap<>();
        for (EvaluatedQuestion eq : evaluatedQuestions) {
            questionDimensions.put(eq.id(), eq.programDimension());
            questionScopes.put(eq.id(), eq.scopeOfFocus());
        }

        AnswerCheckResult checkResult = AnswerCheckResult.fromResults(
                answerResults,
                questionDimensions,
                questionScopes
        );

        evaluatedProjects.add(new EvaluatedProject(
                thisProjectName,
                evaluatedQuestions,
                checkResult
        ));
    }

    /**
     * Evaluates an exported answer and creates an EvaluatedQuestion.
     * Constructs a QuestionDTO from the export data and delegates to AnswerChecker
     * for type-specific evaluation with optional LLM support.
     */
    private EvaluatedQuestion evaluateAndCreateQuestion(ExportedAnswer exported, AnswerChecker answerChecker) {
        // Determine question type, with fallback
        QuestionType questionType = QuestionType.MULTIPLE_CHOICE;
        if (exported.questionType() != null) {
            try {
                questionType = QuestionType.valueOf(exported.questionType());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        // Determine dimension and scope from export data (with fallback)
        ProgramDimension dimension = ProgramDimension.TEXT;
        if (exported.dimension() != null) {
            try {
                dimension = ProgramDimension.valueOf(exported.dimension());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        ScopeOfFocus scope = ScopeOfFocus.BLOCK;
        if (exported.scope() != null) {
            try {
                scope = ScopeOfFocus.valueOf(exported.scope());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        String blockModelCell = dimension.name() + "/" + scope.name();

        // Construct a QuestionDTO from the exported data for the AnswerChecker
        QuestionDTO questionDTO = new QuestionDTO(
                exported.questionId(),
                exported.finder(),
                exported.finder(),
                questionType,
                dimension,
                scope,
                exported.sprite(),
                exported.questionText(),
                null,   // translatableQuestionText
                null,   // translatableChoices
                exported.correctAnswers(),
                exported.scratchBlocks(),
                null    // hint
        );

        // Check the answer using the AnswerChecker (handles all types + LLM for free text)
        String studentAnswer = exported.studentAnswer();
        AnswerResult result;
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            // Unanswered question — generate a hint via LLM if available
            String expectedForUnanswered = exported.correctAnswers() != null && !exported.correctAnswers().isEmpty()
                    ? exported.correctAnswers().stream()
                    .map(this::resolveTranslatableText)
                    .collect(Collectors.joining(", "))
                    : null;
            String hint = null;
            if (llmEvaluator != null) {
                hint = llmEvaluator.generateHint(questionDTO);
            }

            if (hint != null) {
                result = AnswerResult.withLLM(
                        exported.questionId(),
                        questionType,
                        null,
                        expectedForUnanswered,
                        LLMEvaluation.incorrect(0.5, hint)
                );
            } else {
                result = AnswerResult.withFeedback(
                        exported.questionId(), questionType, false, null, expectedForUnanswered, null);
            }
        } else {
            result = answerChecker.checkSingleAnswer(questionDTO, studentAnswer);

            // For non-free-text questions that are incorrect, generate LLM feedback if available.
            // Free text questions already get LLM feedback via AnswerChecker.
            if (!result.correct() && llmEvaluator != null && result.llmEvaluation() == null) {
                LLMEvaluation llmResult = llmEvaluator.evaluate(questionDTO, studentAnswer);
                // Keep the deterministic correctness from AnswerChecker, use LLM feedback
                result = new AnswerResult(
                        result.questionId(),
                        result.questionType(),
                        result.correct(),
                        result.providedAnswer(),
                        result.expectedAnswer(),
                        result.partialCredit(),
                        llmResult.feedback(),
                        llmResult
                );
            }
        }

        String expectedAnswer = exported.correctAnswers() != null && !exported.correctAnswers().isEmpty()
                ? exported.correctAnswers().stream()
                .map(this::resolveTranslatableText)
                .collect(Collectors.joining(", "))
                : "";

        return new EvaluatedQuestion(
                exported.questionId(),
                exported.finder(),
                exported.finder(),
                questionType,
                dimension,
                scope,
                blockModelCell,
                formatAnswerText(exported.questionText()),
                exported.scratchBlocks(),
                formatAnswerText(exported.studentAnswer()),
                formatAnswerText(result.expectedAnswer() != null ? result.expectedAnswer() : expectedAnswer),
                result.correct(),
                result.partialCredit(),
                formatFeedbackText(result.feedback()),
                result.llmEvaluation()
        );
    }

    private String resolveTranslatableText(TranslatableText text) {
        if (text == null) {
            return "";
        }
        return text.resolveText(translator, translator::getInfo);
    }


    /**
     * Formats feedback text for display, converting scratchblocks markers to HTML.
     */
    private String formatFeedbackText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return convertScratchblocksToHtml(escapeHtml(text));
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Converts scratchblocks marker tags to HTML elements.
     * Handles block-level ([scratchblocks]), inline ([sbi], [sb]), and variable ([var]) markers.
     */
    private String convertScratchblocksToHtml(String escaped) {
        return escaped
                // Block-level scratchblocks (multiline scripts) -> <pre class="blocks">
                .replaceAll("(?s)\\[scratchblocks]\\s*(.*?)\\s*\\[/scratchblocks]", "<pre class=\"blocks\">$1</pre>")
                // Inline scratchblocks -> <code class="scratchblocks">
                .replaceAll("(?s)\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>")
                .replaceAll("(?s)\\[sb](.*?)\\[/sb]", "<code class=\"scratchblocks\">$1</code>")
                // Variables -> <code class="scratchblocks"> with variable annotation
                .replaceAll("(?s)\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1 :: variables)</code>")
                // Lists -> <code class="scratchblocks"> with list annotation
                .replaceAll("(?s)\\[list](.*?)\\[/list]", "<code class=\"scratchblocks\">($1 :: list)</code>");
    }

    /**
     * Formats answer text for display, converting scratchblocks markers to HTML.
     */
    private String formatAnswerText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return convertScratchblocksToHtml(escapeHtml(text));
    }



    @Override
    public void finish() throws IOException {
        if (output == null) {
            // Console output
            printConsoleResults();
            return;
        }

        String extension = getFileExtension(output.getFileName().toString());
        if ("json".equalsIgnoreCase(extension)) {
            writeJsonResults();
        } else {
            writeHtmlResults();
        }
    }

    private void printConsoleResults() {
        for (EvaluatedProject project : evaluatedProjects) {
            System.out.println("\n=== Evaluation Results: " + project.projectName() + " ===");
            System.out.printf("Score: %d/%d (%.0f%%)%n",
                    project.checkResult().correctAnswers(),
                    project.checkResult().totalQuestions(),
                    project.checkResult().score() * 100);

            System.out.println("\nResults by question:");
            for (EvaluatedQuestion q : project.questions()) {
                String status = q.correct() ? "✓" : "✗";
                if (!q.correct() && q.partialCredit() > 0) {
                    status = String.format("~%.0f%%", q.partialCredit() * 100);
                }
                System.out.printf("  Q%d [%s]: %s%n", q.id(), status, q.providedAnswer());
                if (!q.correct() && q.expectedAnswer() != null) {
                    System.out.printf("       Expected: %s%n", q.expectedAnswer());
                }
                if (q.feedback() != null) {
                    System.out.printf("       Feedback: %s%n", q.feedback());
                }
            }
        }
    }

    private void writeJsonResults() throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        List<ProjectEvaluation> projectResults = new ArrayList<>();
        for (EvaluatedProject project : evaluatedProjects) {
            List<QuestionResult> questionResults = new ArrayList<>();
            for (EvaluatedQuestion q : project.questions()) {
                questionResults.add(new QuestionResult(
                        q.id(),
                        q.questionType(),
                        q.programDimension(),
                        q.scopeOfFocus(),
                        q.correct(),
                        q.partialCredit(),
                        q.providedAnswer(),
                        q.expectedAnswer(),
                        q.feedback()
                ));
            }

            projectResults.add(new ProjectEvaluation(
                    project.projectName(),
                    project.checkResult().totalQuestions(),
                    project.checkResult().correctAnswers(),
                    project.checkResult().score(),
                    questionResults,
                    project.checkResult().byProgramDimension(),
                    project.checkResult().byScopeOfFocus()
            ));
        }

        EvaluationReport report = new EvaluationReport(
                Instant.now().toString(),
                llmEval,
                projectResults
        );

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
        log.info("Evaluation results written to: " + output);
    }

    private void writeHtmlResults() throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        String scratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
        String translationsJs = loadResource(PATH_TRANSLATIONS_JS);
        String logo = loadResource(LOGO_PATH);
        String faviconBase64 = loadFaviconBase64();

        EvaluationTemplateModel model = new EvaluationTemplateModel(
                translator.getLanguage(),
                faviconBase64,
                logo,
                new Scripts(scratchblocksJs, translationsJs),
                translator,
                llmEval,
                evaluatedProjects
        );

        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(output))) {
            TemplateOutput templateOutput = new PrintWriterOutput(pw);
            templateEngine.render("evaluation_report_template.jte", model, templateOutput);
        }

        log.info("Evaluation report written to: " + output);
    }

    private String loadResource(String path) throws IOException {
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                if (path.equals(LOGO_PATH)) {
                    return "";
                }
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes());
        }
    }

    private String loadFaviconBase64() {
        try (var is = getClass().getResourceAsStream(FAVICON_PATH)) {
            if (is != null) {
                return "data:image/x-icon;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (IOException e) {
            // Ignore
        }
        return "";
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    // Records for template model

    public record EvaluationTemplateModel(
            String language,
            String favicon,
            String logo,
            Scripts scripts,
            IssueTranslator translator,
            boolean llmEval,
            List<EvaluatedProject> projects
    ) {
    }

    public record Scripts(
            String scratchBlocks,
            String scratchBlocksTranslations
    ) {
    }

    public record EvaluatedProject(
            String projectName,
            List<EvaluatedQuestion> questions,
            AnswerCheckResult checkResult
    ) {
    }

    public record EvaluatedQuestion(
            int id,
            String finder,
            String translatedName,
            QuestionType questionType,
            ProgramDimension programDimension,
            ScopeOfFocus scopeOfFocus,
            String blockModelCell,
            String questionText,
            String scratchBlocks,
            String providedAnswer,
            String expectedAnswer,
            boolean correct,
            double partialCredit,
            String feedback,
            LLMEvaluation llmEvaluation
    ) {
    }

    // Records for JSON serialization/deserialization

    private record AnswersInput(
            String projectFile,
            String language,
            List<AnswerInputEntry> answers
    ) {
    }

    /**
     * Record for deserializing answer entries from the export JSON.
     * Now includes all question data for self-contained evaluation.
     */
    private record AnswerInputEntry(
            int questionId,
            String finder,
            String sprite,
            String questionType,
            String questionText,
            String dimension,
            String scope,
            String scratchBlocks,
            List<Choice> choices,
            List<TranslatableText> correctAnswers,
            String studentAnswer,
            // Legacy fields for backward compatibility with old exports
            String answer,
            List<TranslatableText> expectedAnswers
    ) {
        // Helper to get student answer from either new or old field
        String getEffectiveStudentAnswer() {
            return studentAnswer != null ? studentAnswer : answer;
        }

        // Helper to get correct answers from either new or old field
        List<TranslatableText> getEffectiveCorrectAnswers() {
            return correctAnswers != null ? correctAnswers : expectedAnswers;
        }
    }

    /**
     * Record for a choice option in multiple choice questions.
     */
    private record Choice(
            String value,
            String text
    ) {
    }

    /**
     * Record for holding exported answer data for evaluation.
     */
    private record ExportedAnswer(
            int questionId,
            String finder,
            String sprite,
            String questionType,
            String questionText,
            String dimension,
            String scope,
            String scratchBlocks,
            List<Choice> choices,
            List<TranslatableText> correctAnswers,
            String studentAnswer
    ) {
    }

    private record EvaluationReport(
            String evaluatedAt,
            boolean llmEvaluation,
            List<ProjectEvaluation> projects
    ) {
    }

    private record ProjectEvaluation(
            String projectFile,
            int totalQuestions,
            int correctAnswers,
            double score,
            List<QuestionResult> results,
            Map<ProgramDimension, AnswerCheckResult.DimensionScore> byProgramDimension,
            Map<ScopeOfFocus, AnswerCheckResult.DimensionScore> byScopeOfFocus
    ) {
    }

    private record QuestionResult(
            int questionId,
            QuestionType questionType,
            ProgramDimension programDimension,
            ScopeOfFocus scopeOfFocus,
            boolean correct,
            double partialCredit,
            String providedAnswer,
            String expectedAnswer,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String feedback
    ) {
    }
}
