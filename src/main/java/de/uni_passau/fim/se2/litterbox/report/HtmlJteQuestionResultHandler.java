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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.PrintWriterOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Outputs questions as a rich HTML report using JTE templates.
 * Features interactive answer checking and block model navigation.
 *
 * <p>Supports two modes of operation:
 * <ul>
 *   <li><b>File mode:</b> When output is a file path (has .html extension), all projects
 *       are accumulated and written to a single file in {@link #finish()}.</li>
 *   <li><b>Directory mode:</b> When output is a directory (exists as directory or has no
 *       recognized extension), each project is written immediately to a separate file
 *       named {@code <projectname>_questions.html}.</li>
 * </ul>
 */
public class HtmlJteQuestionResultHandler implements QuestionResultHandler {

    private static final Logger log = Logger.getLogger(HtmlJteQuestionResultHandler.class.getName());
    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    private static final TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

    private static final String PATH_SCRATCHBLOCKS_JS = "/js/scratchblocks.min.js";
    private static final String PATH_TRANSLATIONS_JS = "/js/translations-all.js";
    private static final String LOGO_PATH = "/litterbox.svg";
    private static final String FAVICON_PATH = "/favicon.ico";

    private final IssueTranslator translator;
    private final Path output;
    private final boolean showAnswers;
    private final boolean teacherMode;
    private final boolean exportMode;
    private final List<ProjectQuestions> allProjects = new ArrayList<>();
    private final boolean directoryMode;

    // Cached resources for directory mode (loaded lazily)
    private String cachedScratchblocksJs;
    private String cachedTranslationsJs;
    private String cachedLogo;
    private String cachedFaviconBase64;

    public HtmlJteQuestionResultHandler(IssueTranslator translator, Path output,
                                        boolean showAnswers) {
        this(translator, output, showAnswers, false, false);
    }

    public HtmlJteQuestionResultHandler(IssueTranslator translator, Path output,
                                        boolean showAnswers, boolean teacherMode) {
        this(translator, output, showAnswers, teacherMode, false);
    }

    public HtmlJteQuestionResultHandler(IssueTranslator translator, Path output,
                                        boolean showAnswers, boolean teacherMode, boolean exportMode) {
        this.translator = translator;
        this.output = output;
        this.showAnswers = showAnswers;
        this.teacherMode = teacherMode;
        this.exportMode = exportMode;
        this.directoryMode = isDirectoryMode(output);
    }

    private static boolean isDirectoryMode(Path path) {
        if (path == null) {
            return false;
        }
        // If it already exists as a directory, use directory mode
        if (Files.isDirectory(path)) {
            return true;
        }
        // If the filename has no recognized extension, treat as directory
        String filename = path.getFileName().toString();
        return QuestionExportFormat.fromFilename(filename) == null;
    }

    @Override
    public void begin() {
        // Nothing to do
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        String projectName = projectFile.getFileName().toString();
        String projectId = "project-" + projectName.replaceAll("[^a-zA-Z0-9]", "-");

        List<RenderedQuestion> rendered = questions.stream()
                .map(this::renderQuestion)
                .toList();

        Map<String, Integer> coverage = computeCoverage(questions);

        // Collect unique sprites for filtering
        Set<String> sprites = questions.stream()
                .map(QuestionDTO::sprite)
                .filter(s -> s != null && !s.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        // Generate scratchblocks code for each sprite
        Map<String, String> spriteCode = generateSpriteCode(program);

        // Generate sprite data (variables, lists) for each sprite
        Map<String, SpriteData> spriteData = generateSpriteData(program);

        ProjectQuestions projectQuestions = new ProjectQuestions(projectId, projectName,
                rendered, coverage, sprites, spriteCode, spriteData);

        if (directoryMode) {
            // Write immediately for this project
            writeProjectToFile(projectFile, projectQuestions);
        } else {
            // Accumulate for single file output
            allProjects.add(projectQuestions);
        }
    }

    private void writeProjectToFile(Path projectFile, ProjectQuestions projectQuestions) {
        try {
            Files.createDirectories(output);
            String projectBaseName = getProjectBaseName(projectFile);
            Path outputPath = output.resolve(projectBaseName + "_questions.html");

            // Load resources lazily (cached for subsequent projects)
            ensureResourcesLoaded();

            Scripts scripts = new Scripts(cachedScratchblocksJs, cachedTranslationsJs);

            QuestionsTemplateModel model = new QuestionsTemplateModel(
                    translator.getLocale().getLanguage(),
                    cachedFaviconBase64,
                    cachedLogo,
                    scripts,
                    translator,
                    showAnswers,
                    teacherMode,
                    exportMode,
                    List.of(projectQuestions)
            );

            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath))) {
                TemplateOutput templateOutput = new PrintWriterOutput(pw);
                templateEngine.render("questions_report_template.jte", model, templateOutput);
            }

            log.info("Questions report written to: " + outputPath);
        } catch (IOException e) {
            log.severe("Failed to write HTML output for " + projectFile + ": " + e.getMessage());
        }
    }

    private void ensureResourcesLoaded() throws IOException {
        if (cachedScratchblocksJs == null) {
            cachedScratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
            cachedTranslationsJs = loadResource(PATH_TRANSLATIONS_JS);
            cachedLogo = loadResource(LOGO_PATH);
            cachedFaviconBase64 = loadFaviconBase64();
        }
    }

    private String getProjectBaseName(Path projectFile) {
        String fileName = projectFile.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private Map<String, String> generateSpriteCode(Program program) {
        Map<String, String> spriteCode = new LinkedHashMap<>();
        if (program == null) {
            return spriteCode;
        }

        for (var actor : program.getActorDefinitionList().getDefinitions()) {
            String spriteName = actor.getIdent().getName();

            // Generate scratchblocks code for scripts only (no declarations)
            ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(false, true);
            visitor.setProgram(program);
            actor.accept(visitor);
            String code = visitor.getScratchBlocks();
            if (code != null && !code.trim().isEmpty()) {
                // Clean up the scratchblocks markers for display
                code = code.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
                spriteCode.put(spriteName, code);
            } else {
                // Include sprite even if it has no code (might have variables)
                // This ensures Stage and other sprites with only variables are shown
                spriteCode.put(spriteName, "");
            }
        }
        return spriteCode;
    }

    private Map<String, SpriteData> generateSpriteData(Program program) {
        Map<String, SpriteData> spriteData = new LinkedHashMap<>();
        if (program == null) {
            return spriteData;
        }

        for (var actor : program.getActorDefinitionList().getDefinitions()) {
            String spriteName = actor.getIdent().getName();
            var decls = actor.getDecls().getDeclarationStmts();

            List<String> variables = new ArrayList<>();
            List<String> lists = new ArrayList<>();

            for (var decl : decls) {
                if (decl instanceof DeclarationIdentAsTypeStmt identDecl) {
                    var ident = identDecl.getIdent();

                    if (ident instanceof Variable var) {
                        variables.add(var.getName().getName());
                    } else if (ident instanceof ScratchList list) {
                        lists.add(list.getName().getName());
                    }
                }
            }

            spriteData.put(spriteName, new SpriteData(variables, lists));
        }
        return spriteData;
    }

    @Override
    public void finish() throws IOException {
        if (directoryMode) {
            // In directory mode, files are written in handleResult()
            return;
        }

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        String scratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
        String translationsJs = loadResource(PATH_TRANSLATIONS_JS);
        String logo = loadResource(LOGO_PATH);
        String faviconBase64 = loadFaviconBase64();

        Scripts scripts = new Scripts(scratchblocksJs, translationsJs);

        QuestionsTemplateModel model = new QuestionsTemplateModel(
                translator.getLocale().getLanguage(),
                faviconBase64,
                logo,
                scripts,
                translator,
                showAnswers,
                teacherMode,
                exportMode,
                allProjects
        );

        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(output))) {
            TemplateOutput templateOutput = new PrintWriterOutput(pw);
            templateEngine.render("questions_report_template.jte", model, templateOutput);
        }

        log.info("Questions report written to: " + output);
    }

    private RenderedQuestion renderQuestion(QuestionDTO q) {
        String scratchBlocks = null;
        if (q.scratchBlocksCode() != null && !q.scratchBlocksCode().isEmpty()) {
            scratchBlocks = q.scratchBlocksCode()
                    .replace("[scratchblocks]", "")
                    .replace("[/scratchblocks]", "")
                    .trim();
        }

        // Clean the question text (remove choice/solution markers)
        String questionText = cleanQuestionText(q.questionText());

        // Resolve translatable text to strings
        List<String> choices = q.getChoices(translator);
        List<String> correctAnswers = q.getCorrectAnswers(translator);

        // Build complete choices list: combine distractors (choices) with correct answers
        // The question finders generate distractors, so we need to add the correct answer
        List<String> allChoices = new ArrayList<>();
        if (choices != null) {
            allChoices.addAll(choices);
        }
        if (correctAnswers != null) {
            for (String ans : correctAnswers) {
                if (!allChoices.contains(ans)) {
                    allChoices.add(ans);
                }
            }
        }
        // Shuffle choices so correct answer isn't always last
        Collections.shuffle(allChoices);

        // Format choices for display (with scratchblocks rendering) and normalized for data attributes
        List<String> displayChoices = null;
        List<String> normalizedChoices = null;
        if (!allChoices.isEmpty()) {
            displayChoices = allChoices.stream()
                    .map(this::formatScratchBlocksNotation)
                    .toList();
            // Normalized choices: cleaned, lowercase, no newlines - for data-value attribute matching
            normalizedChoices = allChoices.stream()
                    .map(c -> cleanScratchBlocksNotation(c).toLowerCase().trim())
                    .toList();
        }

        // Build answer set for checking (normalized)
        Set<String> answerSet = new HashSet<>();
        if (correctAnswers != null) {
            for (String ans : correctAnswers) {
                answerSet.add(cleanScratchBlocksNotation(ans).toLowerCase().trim());
            }
        }

        // Format correct answers for display (with scratchblocks rendering)
        String formattedAnswers = null;
        if (correctAnswers != null && !correctAnswers.isEmpty()) {
            formattedAnswers = correctAnswers.stream()
                    .map(this::formatScratchBlocksNotation)
                    .collect(Collectors.joining(", "));
        }

        // Determine if this question can be auto-checked
        boolean canCheck = canCheckAnswer(q.questionType(), correctAnswers);

        String translatableAnswersJson = "[]";
        try {
            if (q.translatableCorrectAnswers() != null) {
                translatableAnswersJson = jsonMapper.writeValueAsString(q.translatableCorrectAnswers());
            }
        } catch (JsonProcessingException e) {
            log.warning("Failed to serialize translatable correct answers for question " + q.id());
        }

        return new RenderedQuestion(
                q.id(),
                q.finder(),
                translator.getName(q.finder()),
                q.questionType(),
                q.programDimension(),
                q.scopeOfFocus(),
                q.getBlockModelCell(),
                q.sprite(),
                questionText,
                scratchBlocks,
                normalizedChoices,
                displayChoices,
                allChoices.isEmpty() ? null : allChoices,
                correctAnswers,
                formattedAnswers,
                answerSet,
                q.hint(),
                canCheck,
                translatableAnswersJson
        );
    }

    private boolean canCheckAnswer(QuestionType type, List<String> correctAnswers) {
        // Can only check if there are known correct answers
        boolean hasAnswers = correctAnswers != null && !correctAnswers.isEmpty();

        if (type == QuestionType.MULTIPLE_CHOICE
                || type == QuestionType.NUMBER
                || type == QuestionType.YES_NO
                || type == QuestionType.FREE_TEXT) {
            return hasAnswers;
        }
        return false;
    }

    private Map<String, Integer> computeCoverage(List<QuestionDTO> questions) {
        Map<String, Integer> coverage = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            coverage.merge(q.getBlockModelCell(), 1, Integer::sum);
        }
        return coverage;
    }

    private String cleanQuestionText(String text) {
        if (text == null) {
            return "";
        }
        // Convert scratchblocks notation to HTML for inline rendering
        return formatScratchBlocksNotation(text);
    }

    /**
     * Converts scratchblocks notation markers to HTML code elements for inline rendering.
     * Similar to HTMLReportGenerator.formatHint().
     */
    private String formatScratchBlocksNotation(String text) {
        if (text == null) {
            return "";
        }

        // First escape HTML entities in the entire text, then wrap scratchblocks markers
        // We need to handle this carefully because the content might contain < > characters
        String escaped = escapeHtml(text);

        // Convert inline scratchblocks notation to HTML ((?s) enables DOTALL mode for multiline)
        String formatted = escaped
                .replaceAll("(?s)\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>")
                .replaceAll("(?s)\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1 :: variables)</code>")
                .replaceAll("(?s)\\[list](.*?)\\[/list]", "<code class=\"scratchblocks\">($1 :: list)</code>")
                // Handle block-level scratchblocks (multiline scripts)
                .replaceAll("(?s)\\[scratchblocks]\\s*(.*?)\\s*\\[/scratchblocks]", "<pre class=\"blocks\">$1</pre>");
        // Replace keyword tokens with translated text in styled code elements
        formatted = replaceKeywordToken(formatted, "[IF]", "if () then");
        formatted = replaceKeywordToken(formatted, "[ELSE]", "else");
        // TRUE/FALSE should show as readable text, not as diamond blocks
        formatted = replaceTranslatedToken(formatted, "[TRUE]", "true");
        formatted = replaceTranslatedToken(formatted, "[FALSE]", "false");
        return formatted.trim();
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

    private String replaceKeywordToken(String text, String token, String scratchblock) {
        if (text.contains(token)) {
            return text.replace(token, "<code class=\"scratchblocks\">" + scratchblock + "</code>");
        }
        return text;
    }

    private String replaceTranslatedToken(String text, String token, String key) {
        if (text.contains(token)) {
            String translated = translator.getInfo(key);
            return text.replace(token, "<strong>" + escapeHtml(translated).toUpperCase() + "</strong>");
        }
        return text;
    }

    private String cleanScratchBlocksNotation(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .replaceAll("\\s+", " ")  // Collapse whitespace for consistent matching
                .trim();

        // Strip outer parentheses used by scratchblocks for number/string values
        // e.g., "(0)" -> "0", "(hello)" -> "hello"
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            String inner = cleaned.substring(1, cleaned.length() - 1);
            // Only strip if it's a simple value (no nested parentheses)
            if (!inner.contains("(") && !inner.contains(")")) {
                cleaned = inner;
            }
        }

        return cleaned;
    }

    private String loadResource(String path) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                if (path.equals(LOGO_PATH)) {
                    return "";
                }
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String loadFaviconBase64() {
        try (InputStream is = getClass().getResourceAsStream(FAVICON_PATH)) {
            if (is != null) {
                return "data:image/x-icon;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (IOException e) {
            // Ignore
        }
        return "";
    }

    // Records for template model

    public record QuestionsTemplateModel(
            String language,
            String favicon,
            String logo,
            Scripts scripts,
            IssueTranslator translator,
            boolean showAnswers,
            boolean teacherMode,
            boolean exportMode,
            List<ProjectQuestions> projects
    ) {}

    public record Scripts(
            String scratchBlocks,
            String scratchBlocksTranslations
    ) {}

    public record ProjectQuestions(
            String id,
            String name,
            List<RenderedQuestion> questions,
            Map<String, Integer> coverage,
            Set<String> sprites,
            Map<String, String> spriteCode,
            Map<String, SpriteData> spriteData
    ) {}

    public record SpriteData(
            List<String> variables,
            List<String> lists
    ) {}

    public record RenderedQuestion(
            int id,
            String finder,
            String translatedName,
            QuestionType questionType,
            ProgramDimension programDimension,
            ScopeOfFocus scopeOfFocus,
            String blockModelCell,
            String sprite,
            String questionText,
            String scratchBlocks,
            List<String> normalizedChoices,
            List<String> displayChoices,
            List<String> rawChoices,
            List<String> correctAnswers,
            String formattedCorrectAnswers,
            Set<String> normalizedAnswers,
            String hint,
            boolean canCheck,
            String translatableAnswersJson
    ) {
        /**
         * Returns true if this question expects multiple correct answers to be selected.
         * Used to determine whether to render checkboxes (multi-select) or radio buttons (single-select).
         */
        public boolean isMultiSelect() {
            return correctAnswers != null && correctAnswers.size() > 1;
        }
    }
}
