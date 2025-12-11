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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ComputationalThinkingScore;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ScriptCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.SpriteCount;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramMetricAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.MetricResult;
import java.util.*;
import java.util.stream.Collectors;

public class HTMLReportGenerator implements ReportGenerator {

    private final IssueTranslator translator;
    private final OutputStream outputStream;

    public HTMLReportGenerator(IssueTranslator translator, Path reportFile) throws IOException {
        this(translator, Files.newOutputStream(reportFile));
    }

    public HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream) {
        this.translator = translator;
        this.outputStream = outputStream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        // Delegate single project report to multi-project generation
        generateReport(List.of(new ProjectAnalysisResult("Project", program, issues)));
    }

    public void generateReport(Collection<ProjectAnalysisResult> projects) throws IOException {
        String template = loadTemplate();
        String logoBase64 = loadLogoBase64();
        
        String lang = translator.getLocale().getLanguage();
        
        StringBuilder projectsNav = new StringBuilder();
        StringBuilder projectsContent = new StringBuilder();
        
        boolean singleProject = projects.size() == 1;
        
        // Sort projects by name for consistent order
        List<ProjectAnalysisResult> sortedProjects = new ArrayList<>(projects);
        sortedProjects.sort(Comparator.comparing(ProjectAnalysisResult::projectName));

        boolean first = true;
        for (ProjectAnalysisResult projectResult : sortedProjects) {
            String projectName = projectResult.projectName();
            Program program = projectResult.program();
            Collection<Issue> issues = projectResult.issues();
            
            String projectId = "project-" + projectName.replaceAll("[^a-zA-Z0-9]", "-");
            String activeClass = first ? " active" : "";
            
            if (!singleProject) {
                // Sidebar Navigation Item
                projectsNav.append("<li id=\"nav-item-").append(projectId).append("\" class=\"project-nav-item").append(activeClass).append("\" onclick=\"showProject('").append(projectId).append("')\">")
                           .append(StringEscapeUtils.escapeHtml4(projectName))
                           .append("</li>\n");
            }
            
            // Project Content
            projectsContent.append("<div id=\"").append(projectId).append("\" class=\"project-content").append(activeClass).append("\">\n");
            if (!singleProject) {
                 projectsContent.append("<h2 class=\"project-title\">").append(StringEscapeUtils.escapeHtml4(projectName)).append("</h2>\n");
            }
            projectsContent.append(generateStats(program, issues));
            projectsContent.append(generateContent(program, issues, projectId));
            projectsContent.append(generateMetricsContent(program, projectId));
            projectsContent.append("</div>\n");
            
            first = false;
        }
        
        String bodyClass = singleProject ? "single-project" : "";
        String headerContent = "";
        
        if (singleProject) {
             String projectName = sortedProjects.get(0).projectName();
             headerContent = "<header class=\"report-header\"><img src=\"{{LOGO}}\" class=\"header-logo\"> <h1>Code Report for " + StringEscapeUtils.escapeHtml4(projectName) + "</h1></header>";
        }

        String report = template
                .replace("{{LANG}}", lang)
                .replace("{{LOGO}}", logoBase64)
                .replace("{{BODY_CLASS}}", bodyClass)
                .replace("{{HEADER_CONTENT}}", headerContent)
                .replace("{{PROJECTS_NAV}}", projectsNav.toString())
                .replace("{{PROJECTS_CONTENT}}", projectsContent.toString())
                .replace("{{SCRATCH_LANG}}", lang); // Replacements cascade, so {{LOGO}} in headerContent works if we replace LOGO after? 
                // Wait, string replacement is sequential on the result.
                // If I replace {{HEADER_CONTENT}} first, then {{LOGO}} replacement will catch the one inside headerContent.
                // But my code replaces LOGIN then HEADER_CONTENT.
                // So the {{LOGO}} inside headerContent won't be replaced if I put it in after.
                // I should replace HEADER_CONTENT first.
                
        // Re-ordering replacements:
        report = template
                .replace("{{LANG}}", lang)
                .replace("{{BODY_CLASS}}", bodyClass)
                .replace("{{HEADER_CONTENT}}", headerContent) // Now contains {{LOGO}}
                .replace("{{LOGO}}", logoBase64) // Replaces all {{LOGO}} instances
                .replace("{{PROJECTS_NAV}}", projectsNav.toString())
                .replace("{{PROJECTS_CONTENT}}", projectsContent.toString())
                .replace("{{SCRATCH_LANG}}", lang);

        outputStream.write(report.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    private String loadTemplate() throws IOException {
        try (var is = getClass().getResourceAsStream("/report_template.html")) {
            if (is == null) {
                throw new IOException("Template file not found: /report_template.html");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String loadLogoBase64() {
        try (var is = getClass().getResourceAsStream("/litterbox.png")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            // ignore
        }
        return "";
    }

    private String generateStats(Program program, Collection<Issue> issues) {
        int spriteCount = (int) new SpriteCount<>().calculateMetric(program);
        int scriptCount = (int) new ScriptCount<>().calculateMetric(program);
        int blockCount = (int) new BlockCount<>().calculateMetric(program);
        double ctScore = new ComputationalThinkingScore().calculateMetric(program);

        long bugCount = issues.stream().filter(i -> i.getIssueType() == IssueType.BUG).count();
        long smellCount = issues.stream().filter(i -> i.getIssueType() == IssueType.SMELL).count();
        long perfumeCount = issues.stream().filter(i -> i.getIssueType() == IssueType.PERFUME).count();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"stats-container\">\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(spriteCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("stats_sprites")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(scriptCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("stats_scripts")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(blockCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("stats_blocks")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(bugCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("label_bugs")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(smellCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("label_smells")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(perfumeCount).append("</span><span class=\"stat-label\">").append(translator.getInfo("label_perfumes")).append("</span></div>\n");
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(String.format("%.1f", ctScore)).append("</span><span class=\"stat-label\">").append(translator.getInfo("stats_ct_score")).append("</span></div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }

    private String generateContent(Program program, Collection<Issue> issues, String projectId) {
        if (issues.isEmpty()) {
            return "<p style=\"text-align: center; color: #28a745;\">No issues found!</p>\n";
        }

        StringBuilder sb = new StringBuilder();
        
        // Filter UI
        sb.append("<div class=\"filter-container\" id=\"filter-ui-").append(projectId).append("\">\n");
        sb.append("<div class=\"filter-row\">\n");
        
        // Type checkboxes
        sb.append("<div class=\"filter-group\">\n");
        sb.append("<label>").append(translator.getInfo("filter_by_type")).append("</label>\n");
        sb.append("<div class=\"checkbox-group\">\n");
        sb.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-bug-").append(projectId).append("\" checked onchange=\"applyFilters('").append(projectId).append("')\"> <label for=\"filter-bug-").append(projectId).append("\">").append(translator.getInfo("label_bugs")).append("</label></div>\n");
        sb.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-smell-").append(projectId).append("\" checked onchange=\"applyFilters('").append(projectId).append("')\"> <label for=\"filter-smell-").append(projectId).append("\">").append(translator.getInfo("label_smells")).append("</label></div>\n");
        sb.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-perfume-").append(projectId).append("\" checked onchange=\"applyFilters('").append(projectId).append("')\"> <label for=\"filter-perfume-").append(projectId).append("\">").append(translator.getInfo("label_perfumes")).append("</label></div>\n");
        sb.append("</div>\n"); // checkbox-group
        sb.append("</div>\n"); // filter-group

        // Finder dropdown
        sb.append("<div class=\"filter-group\">\n");
        sb.append("<label for=\"finder-select-").append(projectId).append("\">").append(translator.getInfo("filter_by_finder")).append("</label>\n");
        sb.append("<select id=\"finder-select-").append(projectId).append("\" onchange=\"applyFilters('").append(projectId).append("')\" class=\"finder-select\">\n");
        sb.append("<option value=\"all\">").append(translator.getInfo("filter_all_finders")).append("</option>\n");
        sb.append("</select>\n");
        sb.append("</div>\n");

        // Sprite dropdown
        sb.append("<div class=\"filter-group\">\n");
        sb.append("<label for=\"sprite-select-").append(projectId).append("\">").append(translator.getInfo("filter_by_sprite")).append("</label>\n");
        sb.append("<select id=\"sprite-select-").append(projectId).append("\" onchange=\"applyFilters('").append(projectId).append("')\" class=\"sprite-select\">\n");
        sb.append("<option value=\"all\">").append(translator.getInfo("filter_all_sprites")).append("</option>\n");
        sb.append("</select>\n");
        sb.append("</div>\n");

        sb.append("</div>\n"); // filter-row
        sb.append("</div>\n"); // filter-container


        Map<String, List<Issue>> issuesByCategory = issues.stream()
                .collect(Collectors.groupingBy(this::getCategory));

        // Defined order of categories
        List<String> categories = List.of(FinderGroup.BUGS.group(), FinderGroup.SMELLS.group(), FinderGroup.PERFUMES.group());

        // Navigation
        sb.append("<nav class=\"report-nav\"><ul>\n");
        for (String category : categories) {
            if (issuesByCategory.containsKey(category)) {
                sb.append("<li id=\"nav-").append(category).append("-").append(projectId).append("\"><a href=\"#").append(category).append("-").append(projectId).append("\">").append(translator.getInfo(category)).append("</a></li>\n");
            }
        }
        String metricsLabel = translator.getInfo("label_metrics");
        if ("label_metrics".equals(metricsLabel)) metricsLabel = "Metrics";
        sb.append("<li id=\"nav-metrics-").append(projectId).append("\"><a href=\"#metrics-").append(projectId).append("\">").append(StringEscapeUtils.escapeHtml4(metricsLabel)).append("</a></li>\n");
        sb.append("</ul></nav>\n");

        for (String category : categories) {
            if (issuesByCategory.containsKey(category)) {
                sb.append("<div id=\"section-").append(category).append("-").append(projectId).append("\" class=\"issue-section\">\n");
                sb.append("<h2 id=\"").append(category).append("-").append(projectId).append("\">").append(translator.getInfo(category)).append("</h2>\n");
                
                // Render description
                String descriptionKey = "description_" + category;
                try {
                    String description = translator.getInfo(descriptionKey);
                    if (description != null && !description.isEmpty() && !description.startsWith("Missing key")) {
                            sb.append("<p class=\"category-description\">").append(StringEscapeUtils.escapeHtml4(description)).append("</p>\n");
                    }
                } catch (Exception e) {
                    // Ignore missing descriptions
                }
                
                renderIssueGroup(sb, issuesByCategory.get(category));
                sb.append("<div class=\"back-to-top\"><a href=\"#top\">\u2191 ").append(translator.getInfo("back_to_top")).append("</a></div>\n");
                sb.append("</div>\n"); // end section
            }
        }
        
        return sb.toString();
    }

    private String getCategory(Issue issue) {
        return switch(issue.getIssueType()) {
            case BUG -> FinderGroup.BUGS.group();
            case SMELL -> FinderGroup.SMELLS.group();
            case PERFUME -> FinderGroup.PERFUMES.group();
            default -> FinderGroup.QUESTIONS.group();
        };
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
        String category = getCategory(issue);
        String finderName = StringEscapeUtils.escapeHtml4(issue.getTranslatedFinderName(translator)); // For display
        String finderVal = finderName;
        
        String spriteName = "Stage";
        if (issue.getActorName() != null && !issue.getActorName().isEmpty()) {
            spriteName = StringEscapeUtils.escapeHtml4(issue.getActorName());
        }

        html.append("<div class=\"issue\" data-type=\"").append(category).append("\" data-finder=\"").append(finderVal).append("\" data-sprite=\"").append(spriteName).append("\">\n");
        html.append("<div class=\"issue-header\">\n");
        
        String title = finderName;
        if (issue.getActorName() != null && !issue.getActorName().isEmpty()) {
            title += " in " + translator.getInfo("sprite") + " " + spriteName;
        }
        
        html.append("<span class=\"issue-title\">").append(title).append("</span>");
        html.append("<span class=\"severity severity-").append(issue.getSeverity().name()).append("\">").append(issue.getSeverity().name()).append("</span> ");
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
            String translated = translator.getInfo(key); // Assuming key exists in GeneralTerms
            return text.replace(tag, "<code>" + StringEscapeUtils.escapeHtml4(translated) + "</code>");
        }
        return text;
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
    private String generateMetricsContent(Program program, String projectId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"section-metrics-").append(projectId).append("\" class=\"issue-section\">\n");
        String metricsLabel = translator.getInfo("label_metrics");
        if ("label_metrics".equals(metricsLabel)) metricsLabel = "Metrics";
        sb.append("<h2 id=\"metrics-").append(projectId).append("\">").append(StringEscapeUtils.escapeHtml4(metricsLabel)).append("</h2>\n");
        
        // Use a generic description if specific key not present or empty
        String desc = translator.getInfo("description_metrics");
        if (desc == null || desc.isEmpty() || desc.startsWith("Missing key")) {
             desc = "Various metrics calculated for the project.";
        }
        sb.append("<p class=\"category-description\">").append(StringEscapeUtils.escapeHtml4(desc)).append("</p>\n");

        sb.append("<div class=\"metrics-container\">\n");
        
        ProgramMetricAnalyzer analyzer = new ProgramMetricAnalyzer();
        List<MetricResult> results = analyzer.analyze(program);
        
        // Sort results by name
        results.sort(Comparator.comparing(MetricResult::name));

        for (MetricResult result : results) {
            String name = result.name();
            double value = result.value();
            
            // Try to translate the metric name
            String transKey = "metric_" + name;
            String displayName = translator.getInfo(transKey);
            if (displayName == null || displayName.startsWith("Missing key")) {
                displayName = name;
            }

            sb.append("<div class=\"metric-item\">\n");
            sb.append("<span class=\"metric-name\">").append(StringEscapeUtils.escapeHtml4(displayName)).append("</span>");
            sb.append("<span class=\"metric-value\">").append(String.format("%.2f", value)).append("</span>");
            sb.append("</div>\n");
        }
        
        sb.append("</div>\n"); // metrics-container
        sb.append("<div class=\"back-to-top\"><a href=\"#top\">\u2191 ").append(translator.getInfo("back_to_top")).append("</a></div>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
