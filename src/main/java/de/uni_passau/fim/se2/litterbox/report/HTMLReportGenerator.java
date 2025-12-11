/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class HTMLReportGenerator implements ReportGenerator {

    private final IssueTranslator translator;
    private final OutputStream outputStream;
    private final boolean closeStream;

    public HTMLReportGenerator(IssueTranslator translator, Path reportFile) throws IOException {
        this(translator, Files.newOutputStream(reportFile), true);
    }

    public HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream) {
        this(translator, outputStream, false);
    }

    private HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream, boolean closeStream) {
        this.translator = translator;
        this.outputStream = outputStream;
        this.closeStream = closeStream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"").append(translator.getLocale().getLanguage()).append("\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>LitterBox Report</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/scratchblocks@latest/build/scratchblocks.min.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: sans-serif; margin: 2rem; background-color: #f4f4f4; }\n");
        html.append(".container { max-width: 900px; margin: 0 auto; background: #fff; padding: 2rem; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #333; text-align: center; }\n");
        html.append("h2 { color: #555; border-bottom: 2px solid #eee; padding-bottom: 0.5rem; margin-top: 2rem; }\n");
        html.append(".issue { border: 1px solid #ddd; border-radius: 4px; padding: 1rem; margin-bottom: 1rem; background: #fff; }\n");
        html.append(".issue h3 { margin-top: 0; color: #007bff; display: flex; justify-content: space-between; align-items: center; }\n");
        html.append(".issue-meta { font-size: 0.9em; color: #777; margin-bottom: 0.5rem; }\n");
        html.append(".hint { background: #e9ecef; padding: 1rem; border-radius: 4px; margin-bottom: 1rem; }\n");
        html.append("pre.blocks { background: #fff; border: none; padding: 0; overflow-x: auto; }\n");
        html.append("code.scratchblocks { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: bold; padding: 2px 4px; border-radius: 4px; }\n");
        html.append(".severity { font-size: 0.8em; padding: 2px 6px; border-radius: 4px; color: #fff; }\n");
        html.append(".severity-ERROR { background-color: #dc3545; }\n");
        html.append(".severity-WARNING { background-color: #ffc107; color: #000; }\n");
        html.append(".severity-INFO { background-color: #17a2b8; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1>LitterBox Report</h1>\n");

        if (issues.isEmpty()) {
            html.append("<p style=\"text-align: center; color: #28a745;\">No issues found!</p>\n");
        } else {
            Map<String, List<Issue>> issuesByCategory = issues.stream()
                    .collect(Collectors.groupingBy(this::getCategory));

            // Defined order of categories
            List<String> categories = List.of(FinderGroup.BUGS.group(), FinderGroup.SMELLS.group(), FinderGroup.PERFUMES.group());
            for (String category : categories) {
                if (issuesByCategory.containsKey(category)) {
                    html.append("<h2>").append(translator.getInfo(category)).append("</h2>\n");
                    renderIssueGroup(html, issuesByCategory.get(category));
                }
            }
            // Handle any potential unknown categories
            for (String category : issuesByCategory.keySet()) {
                if (!categories.contains(category)) {
                    html.append("<h2>").append(category).append("</h2>\n"); // Fallback
                    renderIssueGroup(html, issuesByCategory.get(category));
                }
            }
        }

        html.append("</div>\n");
        html.append("<script>\n");
        html.append("scratchblocks.renderMatching('pre.blocks', {\n");
        html.append("  style: 'scratch3',\n");
        html.append("  languages: ['").append(translator.getLocale().getLanguage()).append("', 'en']\n");
        html.append("});\n");
        html.append("scratchblocks.renderMatching('code.scratchblocks', {\n");
        html.append("  style: 'scratch3',\n");
        html.append("  inline: true,\n");
        html.append("  languages: ['").append(translator.getLocale().getLanguage()).append("', 'en']\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>");

        outputStream.write(html.toString().getBytes(StandardCharsets.UTF_8));
        if (closeStream) {
            outputStream.close();
        }
    }

    private String getCategory(Issue issue) {
        // Simple heuristic based on known groupings or IssueType if available
        // Assuming we can map issue types/finders to categories.
        // For now, let's rely on FinderGroup semantics if possible, or string matching.
        // Ideally `Issue` or `IssueFinder` has this info.
        // Checking `IssueTool` or similar would be better, but `Issue` usually knows its "type" via `IssueType`.
        // However `IssueType` is an enum like `BUG`, `SMELL`, etc.
        if (issue.getIssueType().name().equals("BUG")) {
            return FinderGroup.BUGS.group();
        } else if (issue.getIssueType().name().equals("SMELL")) {
            return FinderGroup.SMELLS.group();
        } else if (issue.getIssueType().name().equals("PERFUME")) {
            return FinderGroup.PERFUMES.group();
        }
        return "Other";
    }

    private void renderIssueGroup(StringBuilder html, List<Issue> issues) {
        Map<String, List<Issue>> issuesByFinder = issues.stream()
                .collect(Collectors.groupingBy(Issue::getFinderName));

        for (Map.Entry<String, List<Issue>> entry : issuesByFinder.entrySet()) {
            for (Issue issue : entry.getValue()) {
                renderIssue(html, issue);
            }
        }
    }

    private void renderIssue(StringBuilder html, Issue issue) {
        html.append("<div class=\"issue\">\n");
        html.append("<h3>");
        html.append(StringEscapeUtils.escapeHtml4(issue.getTranslatedFinderName(translator)));
        html.append("<span class=\"severity severity-").append(issue.getSeverity()).append("\">").append(issue.getSeverity()).append("</span>");
        html.append("</h3>\n");
        
        html.append("<div class=\"issue-meta\">");
        html.append("<strong>Sprite:</strong> ").append(StringEscapeUtils.escapeHtml4(issue.getActorName()));
        String location = getIssueLocation(issue);
        if (location != null) {
            html.append(" | <strong>Block ID:</strong> ").append(location);
        }
        html.append("</div>\n");

        html.append("<div class=\"hint\">\n");
        html.append(formatHint(issue.getHintText(translator)));
        html.append("</div>\n");

        String scratchCode;
         if (issue instanceof MultiBlockIssue multi) {
            scratchCode = getScratchBlocksCode(issue, multi.getScriptEntities());
        } else {
            scratchCode = getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());
        }

        if (scratchCode != null && !scratchCode.isEmpty()) {
            html.append("<pre class=\"blocks\">");
            // Remove [scratchblocks] tags since we put it in pre tag directly
            String cleanCode = scratchCode.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
            html.append(StringEscapeUtils.escapeHtml4(cleanCode));
            html.append("</pre>\n");
        }

        html.append("</div>\n");
    }

    private String formatHint(String hint) {
        String escaped = StringEscapeUtils.escapeHtml4(hint);
        
        // Restore tags that we want to process
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

        // Handle translated tokens
        escaped = replaceToken(escaped, "[TRUE]", "true");
        escaped = replaceToken(escaped, "[FALSE]", "false");
        escaped = replaceToken(escaped, "[IF]", "if");
        escaped = replaceToken(escaped, "[ELSE]", "else");

        // Handle Scratchblocks specific tags
        escaped = escaped.replaceAll("\\[sbi\\](.*?)\\[/sbi\\]", "<code class=\"scratchblocks\">$1</code>");
        escaped = escaped.replaceAll("\\[var\\](.*?)\\[/var\\]", "<code class=\"scratchblocks\">($1)</code>"); // Variable shape (round)
        escaped = escaped.replaceAll("\\[list\\](.*?)\\[/list\\]", "<code class=\"scratchblocks\">($1 :: list)</code>"); // List shape (usually variable shape but we can distinguish if needed, lists in scratchblocks often just variable blocks)

        return escaped;
    }

    private String replaceToken(String text, String tag, String key) {
        if (text.contains(tag)) {
            String translated = translator.getName(key); // Assuming key exists in GeneralTerms
            return text.replace(tag, "<code>" + StringEscapeUtils.escapeHtml4(translated) + "</code>");
        }
        return text;
    }

    private String getIssueLocation(final Issue issue) {
        if (issue.getCodeLocation() == null) {
            return null;
        } else {
            return AstNodeUtil.getBlockId(issue.getCodeLocation());
        }
    }

    private String getScratchBlocksCode(final Issue issue, final ScriptEntity location) {
        if (location == null) {
            return "";
        } else {
            final ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
            blockVisitor.begin();
            location.accept(blockVisitor);
            blockVisitor.end();
            return blockVisitor.getScratchBlocks();
        }
    }
    
    private String getScratchBlocksCode(final Issue issue, final List<ScriptEntity> locations) {
        StringBuilder builder = new StringBuilder();
        for (ScriptEntity location : locations) {
            if (location != null) {
                ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
                blockVisitor.begin();
                location.accept(blockVisitor);
                blockVisitor.end();
                String scratchBlockCode = blockVisitor.getScratchBlocks();
                builder.append(scratchBlockCode);
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
