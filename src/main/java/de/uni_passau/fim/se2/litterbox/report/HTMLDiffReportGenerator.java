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

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import de.uni_passau.fim.se2.litterbox.analytics.DiffResult;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.diff.ProjectCodeDiff;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.PrintWriterOutput;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Generates an HTML report for diff analysis results.
 *
 * <p>The report categorizes issues into four groups: NEW, FIXED, REMOVED, and PERSISTING,
 * matching the console output structure of the diff command.
 */
public class HTMLDiffReportGenerator {

    private static final TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

    private static final Escaper HTML_ESCAPER = HtmlEscapers.htmlEscaper();

    private final IssueTranslator translator;
    private final OutputStream outputStream;

    private static final String LOGO_PATH = "/litterbox.svg";
    private static final String FAVICON_PATH = "/favicon.ico";

    private static final String PATH_SCRATCHBLOCKS_JS = "/js/scratchblocks.min.js";
    private static final String PATH_TRANSLATIONS_JS = "/js/translations-all.js";

    public HTMLDiffReportGenerator(IssueTranslator translator, Path reportFile) throws IOException {
        this(translator, Files.newOutputStream(reportFile));
    }

    public HTMLDiffReportGenerator(IssueTranslator translator, OutputStream outputStream) {
        this.translator = translator;
        this.outputStream = outputStream;
    }

    /**
     * Generates the HTML diff report.
     *
     * @param result the diff analysis result
     * @throws IOException if writing fails
     */
    public void generateReport(DiffResult result) throws IOException {
        final String logo = loadResource(LOGO_PATH);
        final String faviconBase64 = loadFaviconBase64();
        final Scripts scripts = buildScripts();

        final List<DiffCategory> categories = buildCategories(result);
        final DiffSummary summary = new DiffSummary(
                result.fixedCount(),
                result.newCount(),
                result.persistingCount(),
                result.removedCount()
        );

        try (var pw = new PrintWriter(outputStream)) {
            final TemplateOutput output = new PrintWriterOutput(pw);
            final DiffTemplateModel model = new DiffTemplateModel(
                    translator.getLocale().getLanguage(),
                    faviconBase64,
                    logo,
                    scripts,
                    translator,
                    result.beforeSource(),
                    result.afterSource(),
                    summary,
                    categories,
                    result.projectDiff()
            );
            templateEngine.render("diff_report_template.jte", model, output);
        }
    }

    private Scripts buildScripts() throws IOException {
        final String scratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
        final String translationsJs = loadResource(PATH_TRANSLATIONS_JS);

        return new Scripts(scratchblocksJs, translationsJs);
    }

    private List<DiffCategory> buildCategories(DiffResult result) {
        final List<DiffCategory> categories = new ArrayList<>();

        // NEW bugs - use after version for hints
        if (!result.newIssues().isEmpty()) {
            categories.add(new DiffCategory(
                    "NEW",
                    "new_bugs",
                    "✗",
                    "category-new",
                    buildRenderedIssues(result.newIssues(), false, "NEW")
            ));
        }

        // FIXED bugs - use before version for hints (original issue data)
        if (!result.fixed().isEmpty()) {
            categories.add(new DiffCategory(
                    "FIXED",
                    "fixed_bugs",
                    "✓",
                    "category-fixed",
                    buildRenderedIssues(result.fixed(), true, "FIXED")
            ));
        }

        // PERSISTING bugs - use after version for hints
        if (!result.persisting().isEmpty()) {
            categories.add(new DiffCategory(
                    "PERSISTING",
                    "persisting_bugs",
                    "-",
                    "category-persisting",
                    buildRenderedIssues(result.persisting(), false, "PERSISTING")
            ));
        }

        // REMOVED bugs - use before version for hints (original issue data)
        if (!result.removed().isEmpty()) {
            categories.add(new DiffCategory(
                    "REMOVED",
                    "removed_bugs",
                    "?",
                    "category-removed",
                    buildRenderedIssues(result.removed(), true, "REMOVED")
            ));
        }

        return categories;
    }

    private List<RenderedDiffIssue> buildRenderedIssues(List<Issue> issues, boolean useOriginalData, String categoryId) {
        return issues.stream()
                .map(issue -> convertIssue(issue, useOriginalData, categoryId))
                .toList();
    }

    private RenderedDiffIssue convertIssue(Issue issue, boolean useOriginalData, String categoryId) {
        // Get before code (from original snapshot)
        String beforeCode = issue.getOriginalCodeSnippet();
        if (beforeCode == null && issue.getCodeLocation() != null) {
            beforeCode = getScratchBlocksFromCurrentAst(issue);
        }

        if (beforeCode != null) {
            beforeCode = beforeCode.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
        }

        // Get after code (from current AST)
        String afterCode = getScratchBlocksFromCurrentAst(issue);
        if (afterCode != null) {
            afterCode = afterCode.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
        }

        // The Hint object is stored on the Issue, so getHintText() works for all cases
        String hintText = issue.getHintText(translator);

        return new RenderedDiffIssue(
                issue.getId(),
                issue.getFinderName(),
                issue.getTranslatedFinderName(translator),
                issue.getActorName(),
                formatHint(hintText),
                beforeCode,
                afterCode,
                categoryId,
                getSeverityClass(issue),
                issue.getSeverity().name(),
                getSeverityIcon(issue)
        );
    }

    private String getScratchBlocksFromCurrentAst(Issue issue) {
        if (issue instanceof MultiBlockIssue multi) {
            return getScratchBlocksCode(issue, multi.getScriptEntities());
        }
        return getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());
    }

    private String getScratchBlocksCode(Issue issue, ScriptEntity location) {
        if (location == null) {
            return "";
        }
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor(issue);
        visitor.begin();
        location.accept(visitor);
        visitor.end();
        return visitor.getScratchBlocks();
    }

    private String getScratchBlocksCode(Issue issue, List<ScriptEntity> locations) {
        StringBuilder sb = new StringBuilder();
        for (ScriptEntity loc : locations) {
            sb.append(getScratchBlocksCode(issue, loc)).append("\n");
        }
        return sb.toString();
    }

    private String formatHint(String hint) {
        if (hint == null) {
            return "";
        }
        String escaped = HTML_ESCAPER.escape(hint);
        escaped = escaped.replace("&lt;", "<").replace("&gt;", ">")
                .replace("[b]", "<b>").replace("[/b]", "</b>")
                .replace("[i]", "<i>").replace("[/i]", "</i>")
                .replace("[u]", "<u>").replace("[/u]", "</u>")
                .replace("[s]", "<s>").replace("[/s]", "</s>")
                .replace("[bc]", "<b><code>").replace("[/bc]", "</code></b>")
                .replace("[newLine]", "<br>")
                .replace("[LEQ]", "<code>&lt;</code>")
                .replace("[GEQ]", "<code>&gt;</code>")
                .replace("[EQ]", "<code>=</code>");

        escaped = replaceToken(escaped, "[TRUE]", "true");
        escaped = replaceToken(escaped, "[FALSE]", "false");
        escaped = replaceToken(escaped, "[IF]", "if");
        escaped = replaceToken(escaped, "[ELSE]", "else");

        escaped = escaped.replaceAll("\\[sbi](.*?)\\[/sbi]", "<code class=\"scratchblocks\">$1</code>");
        escaped = escaped.replaceAll("\\[var](.*?)\\[/var]", "<code class=\"scratchblocks\">($1)</code>");
        escaped = escaped.replaceAll("\\[list](.*?)\\[/list]", "<code class=\"scratchblocks\">($1 :: list)</code>");

        return escaped;
    }

    private String replaceToken(String text, String tag, String key) {
        if (text.contains(tag)) {
            String translated = translator.getInfo(key);
            return text.replace(tag, "<code>" + HTML_ESCAPER.escape(translated) + "</code>");
        }
        return text;
    }

    private String loadResource(String path) throws IOException {
        try (var is = getClass().getResourceAsStream(path)) {
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
        try (var is = getClass().getResourceAsStream(FAVICON_PATH)) {
            if (is != null) {
                return "data:image/x-icon;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (IOException e) { /* ignore */ }
        return "";
    }

    private String getSeverityClass(final Issue issue) {
        if (issue.getIssueType() == IssueType.PERFUME) {
            return "severity-perfume-" + issue.getSeverity().name();
        } else {
            return "severity-" + issue.getSeverity().name();
        }
    }

    private String getSeverityIcon(final Issue issue) {
        if (issue.getIssueType() == IssueType.PERFUME) {
            return switch (issue.getSeverity()) {
                case HIGH -> "&#127942;"; // Trophy
                case MEDIUM -> "&#11088;"; // Star
                case LOW -> "&#10024;"; // Sparkles
            };
        }
        return switch (issue.getSeverity()) {
            case HIGH -> "&#128308;"; // Red Circle
            case MEDIUM -> "&#9888;"; // Warning Sign
            case LOW -> "&#8505;"; // Information Source
        };
    }

    // Template model records

    public record DiffTemplateModel(
            String language,
            String favicon,
            String logo,
            Scripts scripts,
            IssueTranslator translator,
            String beforeSource,
            String afterSource,
            DiffSummary summary,
            List<DiffCategory> categories,
            ProjectCodeDiff projectCodeDiff
    ) {}

    public record Scripts(
            String scratchBlocks,
            String scratchBlocksTranslations
    ) {}

    public record DiffSummary(
            int fixed,
            int newBugs,
            int persisting,
            int removed
    ) {}

    public record DiffCategory(
            String id,
            String labelKey,
            String marker,
            String cssClass,
            List<RenderedDiffIssue> issues
    ) {}

    public record RenderedDiffIssue(
            int id,
            String finder,
            String translatedFinderName,
            String sprite,
            String hintText,
            @Nullable String beforeScratchBlocks,
            @Nullable String afterScratchBlocks,
            String categoryId,
            String severityClass,
            String severity,
            String severityIcon
    ) {
        public boolean isCodeUnchanged() {
            if (beforeScratchBlocks == null || afterScratchBlocks == null) {
                return false;
            }
            return beforeScratchBlocks.trim().equals(afterScratchBlocks.trim());
        }
    }
}
