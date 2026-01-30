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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    private final AnswerChecker answerChecker;

    private Map<Integer, String> studentAnswers;
    private Map<Integer, List<String>> exportedExpectedAnswers;
    private String projectFileName;
    private final List<EvaluatedProject> evaluatedProjects = new ArrayList<>();

    public EvaluationResultHandler(IssueTranslator translator, Path output, Path answersFile, boolean llmEval) {
        this.translator = translator;
        this.output = output;
        this.answersFile = answersFile;
        this.llmEval = llmEval;
        this.objectMapper = new ObjectMapper();

        String language = translator.getLocale().getLanguage();
        LLMAnswerEvaluator llmEvaluator = llmEval ? new LLMAnswerEvaluator(language) : null;
        this.answerChecker = new AnswerChecker(llmEvaluator, 0.0, translator);
    }

    @Override
    public void begin() throws IOException {
        // Load answers from JSON file
        loadAnswers();
    }

    private void loadAnswers() throws IOException {
        JsonNode root = objectMapper.readTree(answersFile.toFile());

        projectFileName = root.has("projectFile") ? root.get("projectFile").asText() : null;

        studentAnswers = new HashMap<>();
        exportedExpectedAnswers = new HashMap<>();
        JsonNode answersNode = root.get("answers");
        if (answersNode != null && answersNode.isArray()) {
            for (JsonNode answer : answersNode) {
                int questionId = answer.get("questionId").asInt();
                String answerText = answer.get("answer").asText();
                studentAnswers.put(questionId, answerText);

                // Load expected answers if present (for consistent evaluation)
                if (answer.has("expectedAnswers")) {
                    List<String> expected = new ArrayList<>();
                    for (JsonNode exp : answer.get("expectedAnswers")) {
                        expected.add(exp.asText());
                    }
                    if (!expected.isEmpty()) {
                        exportedExpectedAnswers.put(questionId, expected);
                    }
                }
            }
        }
        if (root.has("language")) {
            String lang = root.get("language").asText();
            if (lang != null && !lang.isEmpty()) {
                this.translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(lang));
                log.info("Using language from answer file: " + lang);
            }
        }

        log.info("Loaded " + studentAnswers.size() + " answers from " + answersFile);
        if (!exportedExpectedAnswers.isEmpty()) {
            log.info("Loaded " + exportedExpectedAnswers.size() + " expected answers from export");
        }
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        // Check if this is the project we have answers for
        String thisProjectName = projectFile.getFileName().toString();
        if (projectFileName != null && !thisProjectName.equals(projectFileName)) {
            log.warning("Project name mismatch: expected " + projectFileName + " but got " + thisProjectName);
        }

        // Filter questions to only those that have answers in the export
        // Also override correct answers with exported expected answers if available
        // This ensures consistent evaluation even for questions with random elements
        List<QuestionDTO> answeredQuestions = questions.stream()
                .filter(q -> studentAnswers.containsKey(q.id()) || exportedExpectedAnswers.containsKey(q.id()))
                .map(q -> {
                    List<String> exportedAnswers = exportedExpectedAnswers.get(q.id());
                    if (exportedAnswers != null && !exportedAnswers.isEmpty()) {
                        // Use the exported expected answers instead of regenerated ones
                        log.fine("Question " + q.id() + " (" + q.finder() + "): using exported expected answers: " + exportedAnswers);
                        return new QuestionDTO(
                                q.id(), q.finder(), q.name(), q.questionType(),
                                q.programDimension(), q.scopeOfFocus(), q.sprite(),
                                q.questionText(), q.choices(), exportedAnswers,
                                q.scratchBlocksCode(), q.hint()
                        );
                    }
                    log.fine("Question " + q.id() + " (" + q.finder() + "): using regenerated answers: " + q.correctAnswers());
                    return q;
                })
                .toList();

        if (exportedExpectedAnswers.isEmpty()) {
            log.warning("No expectedAnswers found in export file. Using regenerated question answers. "
                    + "For consistent evaluation, re-export answers with the latest version.");
        }

        // Evaluate answers only for answered questions
        AnswerCheckResult checkResult = answerChecker.checkAnswers(answeredQuestions, studentAnswers);

        // Create evaluated questions for template
        List<EvaluatedQuestion> evaluatedQuestions = new ArrayList<>();
        for (int i = 0; i < answeredQuestions.size(); i++) {
            QuestionDTO question = answeredQuestions.get(i);
            AnswerResult result = checkResult.results().get(i);
            evaluatedQuestions.add(createEvaluatedQuestion(question, result));
        }

        evaluatedProjects.add(new EvaluatedProject(
                thisProjectName,
                evaluatedQuestions,
                checkResult
        ));
    }

    private EvaluatedQuestion createEvaluatedQuestion(QuestionDTO question, AnswerResult result) {
        String scratchBlocks = null;
        if (question.scratchBlocksCode() != null && !question.scratchBlocksCode().isEmpty()) {
            scratchBlocks = escapeHtml(question.scratchBlocksCode()
                    .replace("[scratchblocks]", "")
                    .replace("[/scratchblocks]", "")
                    .trim());
        }

        String cleanedQuestion = cleanQuestionText(question.questionText());

        return new EvaluatedQuestion(
                question.id(),
                question.finder(),
                translator.getName(question.finder()),
                question.questionType(),
                question.programDimension(),
                question.scopeOfFocus(),
                question.getBlockModelCell(),
                cleanedQuestion,
                scratchBlocks,
                formatAnswerText(result.providedAnswer()),
                formatAnswerText(result.expectedAnswer()),
                result.correct(),
                result.partialCredit(),
                formatFeedbackText(result.feedback()),
                result.llmEvaluation()
        );
    }

    /**
     * Formats feedback text for display, converting scratchblocks markers to HTML.
     */
    private String formatFeedbackText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Escape HTML entities first
        String escaped = escapeHtml(text);

        // Convert scratchblocks markers to inline HTML code elements
        return escaped
                .replaceAll("(?s)\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>")
                .replaceAll("(?s)\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1 :: variables)</code>");
    }

    /**
     * Formats answer text for display, converting scratchblocks markers to HTML
     * and wrapping code-like answers in scratchblocks tags.
     */
    private String formatAnswerText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // First escape HTML entities so that scratchblocks syntax like < > is preserved
        String escaped = escapeHtml(text);

        // Convert explicit markers to HTML elements
        String result = escaped
                // Block-level scratchblocks (multiline scripts) -> <pre class="blocks">
                .replaceAll("(?s)\\[scratchblocks]\\s*(.*?)\\s*\\[/scratchblocks]", "<pre class=\"blocks\">$1</pre>")
                // Inline scratchblocks -> <code class="scratchblocks">
                .replaceAll("(?s)\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>")
                .replaceAll("(?s)\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1 :: variables)</code>");

        // If no markers were found and the text looks like scratchblocks code
        // (contains typical scratchblocks keywords), wrap it
        if (result.equals(escaped) && looksLikeScratchblocks(text)) {
            // Multi-line code should use <pre class="blocks">, single-line uses <code>
            if (text.contains("\n")) {
                return "<pre class=\"blocks\">" + escaped + "</pre>";
            }
            return "<code class=\"scratchblocks\">" + escaped + "</code>";
        }

        return result;
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
     * Checks if text looks like scratchblocks code based on common patterns.
     */
    private boolean looksLikeScratchblocks(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase();
        // Common scratchblocks patterns
        return lower.contains("when ")
                || lower.contains("say ")
                || lower.contains("move ")
                || lower.contains("turn ")
                || lower.contains("go to")
                || lower.contains("glide ")
                || lower.contains("point ")
                || lower.contains("if <")
                || lower.contains("repeat ")
                || lower.contains("forever")
                || lower.contains("broadcast")
                || lower.contains("set [")
                || lower.contains("change [")
                || lower.contains("change x")
                || lower.contains("change y")
                || lower.contains("set x")
                || lower.contains("set y")
                || lower.contains("show")
                || lower.contains("hide")
                || lower.contains("wait ")
                || lower.contains("stop ")
                || lower.contains("ask ")
                || lower.contains("switch costume")
                || lower.contains("next costume")
                || lower.contains("play sound")
                || lower.contains("::");
    }

    private String cleanQuestionText(String text) {
        if (text == null) {
            return "";
        }
        // Escape HTML entities to preserve scratchblocks syntax like < >
        String escaped = escapeHtml(text);

        // Convert scratchblocks markers to HTML elements
        return escaped
                // Block-level scratchblocks (multiline scripts) -> <pre class="blocks">
                .replaceAll("(?s)\\[scratchblocks]\\s*(.*?)\\s*\\[/scratchblocks]", "<pre class=\"blocks\">$1</pre>")
                // Inline scratchblocks -> <code class="scratchblocks">
                .replaceAll("(?s)\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>")
                .replaceAll("(?s)\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1 :: variables)</code>")
                .trim();
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("evaluatedAt", Instant.now().toString());
        result.put("llmEvaluation", llmEval);

        List<Map<String, Object>> projectResults = new ArrayList<>();
        for (EvaluatedProject project : evaluatedProjects) {
            Map<String, Object> pr = new LinkedHashMap<>();
            pr.put("projectFile", project.projectName());
            pr.put("totalQuestions", project.checkResult().totalQuestions());
            pr.put("correctAnswers", project.checkResult().correctAnswers());
            pr.put("score", project.checkResult().score());

            List<Map<String, Object>> questionResults = new ArrayList<>();
            for (EvaluatedQuestion q : project.questions()) {
                Map<String, Object> qr = new LinkedHashMap<>();
                qr.put("questionId", q.id());
                qr.put("questionType", q.questionType().name());
                qr.put("programDimension", q.programDimension().name());
                qr.put("scopeOfFocus", q.scopeOfFocus().name());
                qr.put("correct", q.correct());
                qr.put("partialCredit", q.partialCredit());
                qr.put("providedAnswer", q.providedAnswer());
                qr.put("expectedAnswer", q.expectedAnswer());
                if (q.feedback() != null) {
                    qr.put("feedback", q.feedback());
                }
                questionResults.add(qr);
            }
            pr.put("results", questionResults);

            // Add breakdown by dimension
            Map<String, Object> byDimension = new LinkedHashMap<>();
            for (var entry : project.checkResult().byProgramDimension().entrySet()) {
                Map<String, Object> dimScore = new LinkedHashMap<>();
                dimScore.put("total", entry.getValue().total());
                dimScore.put("correct", entry.getValue().correct());
                byDimension.put(entry.getKey().name(), dimScore);
            }
            pr.put("byProgramDimension", byDimension);

            Map<String, Object> byScope = new LinkedHashMap<>();
            for (var entry : project.checkResult().byScopeOfFocus().entrySet()) {
                Map<String, Object> scopeScore = new LinkedHashMap<>();
                scopeScore.put("total", entry.getValue().total());
                scopeScore.put("correct", entry.getValue().correct());
                byScope.put(entry.getKey().name(), scopeScore);
            }
            pr.put("byScopeOfFocus", byScope);

            projectResults.add(pr);
        }
        result.put("projects", projectResults);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), result);
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
                translator.getLocale().getLanguage(),
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
}
