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

import de.uni_passau.fim.se2.litterbox.analytics.DotGraphAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.GraphType;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramMetricAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ComputationalThinkingScore;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.MetricResult;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ScriptCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.SpriteCount;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

/**
 * Generates an HTML report for the analysis results.
 */
public class HTMLReportGenerator implements ReportGenerator {

    private final IssueTranslator translator;
    private final OutputStream outputStream;

    private static final String TEMPLATE_PATH = "/report_template.html";
    private static final String LOGO_PATH = "/litterbox.png";
    private static final String PLACEHOLDER_LANG = "{{LANG}}";
    private static final String PLACEHOLDER_LOGO = "{{LOGO}}";
    private static final String PLACEHOLDER_BODY_CLASS = "{{BODY_CLASS}}";
    private static final String PLACEHOLDER_HEADER = "{{HEADER_CONTENT}}";
    private static final String PLACEHOLDER_NAV = "{{PROJECTS_NAV}}";
    private static final String PLACEHOLDER_CONTENT = "{{PROJECTS_CONTENT}}";
    private static final String PLACEHOLDER_SCRATCH_LANG = "{{SCRATCH_LANG}}";
    private static final String PLACEHOLDER_SCRATCHBLOCKS_JS = "{{SCRATCHBLOCKS_JS}}";
    private static final String PLACEHOLDER_TRANSLATIONS_JS = "{{TRANSLATIONS_JS}}";
    private static final String PLACEHOLDER_VIZ_JS = "{{VIZ_JS}}";
    private static final String PLACEHOLDER_SVG_PAN_ZOOM_JS = "{{SVG_PAN_ZOOM_JS}}";

    private static final String PATH_SCRATCHBLOCKS_JS = "/js/scratchblocks.min.js";
    private static final String PATH_TRANSLATIONS_JS = "/js/translations-all.js";
    private static final String PATH_VIZ_JS = "/js/viz.js";
    private static final String PATH_SVG_PAN_ZOOM_JS = "/js/svg-pan-zoom.min.js";

    public HTMLReportGenerator(IssueTranslator translator, Path reportFile) throws IOException {
        this(translator, Files.newOutputStream(reportFile));
    }

    public HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream) {
        this.translator = translator;
        this.outputStream = outputStream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        generateReport(List.of(new ProjectAnalysisResult("Project", program, issues)));
    }

    public void generateReport(Collection<ProjectAnalysisResult> projects) throws IOException {
        String template = loadResource(TEMPLATE_PATH);
        String logoBase64 = loadLogoBase64();
        String scratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
        String translationsJs = loadResource(PATH_TRANSLATIONS_JS);
        String vizJs = loadResource(PATH_VIZ_JS);
        String svgPanZoomJs = loadResource(PATH_SVG_PAN_ZOOM_JS);
        String lang = translator.getLocale().getLanguage();
        boolean singleProject = projects.size() == 1;
        List<ProjectAnalysisResult> sortedProjects = sortProjects(projects);

        StringBuilder projectsNav = new StringBuilder();
        StringBuilder projectsContent = new StringBuilder();

        boolean first = true;
        for (ProjectAnalysisResult project : sortedProjects) {
            String projectId = generateProjectId(project.projectName());
            boolean active = first;

            if (!singleProject) {
                appendProjectNavItem(projectsNav, project.projectName(), projectId, active);
            }
            appendProjectContent(projectsContent, project, projectId, active, singleProject);

            first = false;
        }

        String bodyClass = singleProject ? "single-project" : "";
        String headerContent = singleProject ? generateSingleProjectHeader(sortedProjects.get(0).projectName()) : "";

        String report = template
                .replace(PLACEHOLDER_LANG, lang)
                .replace(PLACEHOLDER_BODY_CLASS, bodyClass)
                .replace(PLACEHOLDER_HEADER, headerContent)
                .replace(PLACEHOLDER_LOGO, logoBase64)
                .replace(PLACEHOLDER_NAV, projectsNav.toString())
                .replace(PLACEHOLDER_CONTENT, projectsContent.toString())
                .replace(PLACEHOLDER_SCRATCH_LANG, lang)
                .replace(PLACEHOLDER_SCRATCHBLOCKS_JS, scratchblocksJs)
                .replace(PLACEHOLDER_TRANSLATIONS_JS, translationsJs)
                .replace(PLACEHOLDER_VIZ_JS, vizJs)
                .replace(PLACEHOLDER_SVG_PAN_ZOOM_JS, svgPanZoomJs);

        outputStream.write(report.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    private List<ProjectAnalysisResult> sortProjects(Collection<ProjectAnalysisResult> projects) {
        List<ProjectAnalysisResult> sorted = new ArrayList<>(projects);
        sorted.sort(Comparator.comparing(ProjectAnalysisResult::projectName));
        return sorted;
    }

    private String generateProjectId(String projectName) {
        return "project-" + projectName.replaceAll("[^a-zA-Z0-9]", "-");
    }

    private void appendProjectNavItem(StringBuilder sb, String name, String id, boolean active) {
        String activeClass = active ? " active" : "";
        sb.append("<li id=\"nav-item-").append(id).append("\" class=\"project-nav-item").append(activeClass)
                .append("\" onclick=\"showProject('").append(id).append("')\">")
                .append(StringEscapeUtils.escapeHtml4(name))
                .append("</li>\n");
    }

    private void appendProjectContent(StringBuilder sb, ProjectAnalysisResult project, String id, boolean active, boolean singleProject) {
        String activeClass = active ? " active" : "";
        sb.append("<div id=\"").append(id).append("\" class=\"project-content").append(activeClass).append("\">\n");

        if (!singleProject) {
            sb.append("<h2 class=\"project-title\">").append(StringEscapeUtils.escapeHtml4(project.projectName())).append("</h2>\n");
        }

        sb.append(generateStats(project.program(), project.issues()));
        sb.append(generateIssuesContent(project.program(), project.issues(), id));
        sb.append(generateGraphsContent(project.program(), id));
        sb.append(generateMetricsContent(project.program(), id));
        sb.append("</div>\n");
    }

    private String generateSingleProjectHeader(String projectName) {
        return "<header class=\"report-header\"><img src=\"" + PLACEHOLDER_LOGO + "\" class=\"header-logo\"> <h1>Code Report for "
                + StringEscapeUtils.escapeHtml4(projectName) + "</h1></header>";
    }

    private String generateStats(Program program, Collection<Issue> issues) {
        long bugCount = issues.stream().filter(i -> i.getIssueType() == IssueType.BUG).count();
        long smellCount = issues.stream().filter(i -> i.getIssueType() == IssueType.SMELL).count();
        long perfumeCount = issues.stream().filter(i -> i.getIssueType() == IssueType.PERFUME).count();

        int spriteCount = (int) new SpriteCount<>().calculateMetric(program);
        int scriptCount = (int) new ScriptCount<>().calculateMetric(program);
        int blockCount = (int) new BlockCount<>().calculateMetric(program);
        double ctScore = new ComputationalThinkingScore().calculateMetric(program);

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"stats-container\">\n");
        appendStatBox(sb, String.valueOf(spriteCount), "stats_sprites");
        appendStatBox(sb, String.valueOf(scriptCount), "stats_scripts");
        appendStatBox(sb, String.valueOf(blockCount), "stats_blocks");
        appendStatBox(sb, String.valueOf(bugCount), "label_bugs");
        appendStatBox(sb, String.valueOf(smellCount), "label_smells");
        appendStatBox(sb, String.valueOf(perfumeCount), "label_perfumes");
        appendStatBox(sb, String.format("%.1f", ctScore), "stats_ct_score");
        sb.append("</div>\n");
        return sb.toString();
    }

    private void appendStatBox(StringBuilder sb, String value, String labelKey) {
        sb.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(value)
                .append("</span><span class=\"stat-label\">").append(translator.getInfo(labelKey))
                .append("</span></div>\n");
    }

    private String generateIssuesContent(Program program, Collection<Issue> issues, String projectId) {
        if (issues.isEmpty()) {
            return "<p style=\"text-align: center; color: #28a745;\">" + translator.getInfo("no_issues_found") + "</p>\n";
        }

        StringBuilder sb = new StringBuilder();
        var issuesByCategory = issues.stream().collect(Collectors.groupingBy(this::getCategory));
        List<String> categories = List.of(FinderGroup.BUGS.group(), FinderGroup.SMELLS.group(), FinderGroup.PERFUMES.group());

        // Navigation
        sb.append("<nav class=\"report-nav\"><ul>\n");
        categories.forEach(cat -> appendNavLink(sb, cat + "-" + projectId, translator.getInfo(cat)));
        appendNavLink(sb, "graphs-" + projectId, getTranslatedLabel("label_graphs", "Graphs"));
        appendNavLink(sb, "metrics-" + projectId, getTranslatedLabel("label_metrics", "Metrics"));
        sb.append("</ul></nav>\n");

        for (String category : categories) {
            String content = generateCategoryContent(projectId, category, issuesByCategory.get(category));
            sb.append(createCollapsibleSection(category + "-" + projectId, translator.getInfo(category), content));
        }

        return sb.toString();
    }

    private void appendNavLink(StringBuilder sb, String idSuffix, String label) {
        sb.append("<li id=\"nav-").append(idSuffix).append("\"><a href=\"#").append(idSuffix).append("\">")
                .append(StringEscapeUtils.escapeHtml4(label)).append("</a></li>\n");
    }

    private String generateCategoryContent(String projectId, String category, List<Issue> issues) {
        StringBuilder sb = new StringBuilder();

        String description = getTranslatedInfo("description_" + category);
        if (description != null) {
            sb.append("<p class=\"category-description\">").append(StringEscapeUtils.escapeHtml4(description)).append("</p>\n");
        }

        if (issues != null && !issues.isEmpty()) {
            sb.append(generateSectionFilter(projectId, category, issues));
            Map<String, List<Issue>> issuesByFinder = issues.stream().collect(Collectors.groupingBy(Issue::getFinderName));
            issuesByFinder.values().forEach(list -> list.forEach(issue -> renderIssue(sb, issue)));
        } else {
            sb.append("<p>").append(translator.getInfo("no_issues_found")).append("</p>\n");
        }

        sb.append(createBackToTop());
        return sb.toString();
    }

    private String createCollapsibleSection(String idSuffix, String title, String content) {
        return "<div id=\"section-" + idSuffix + "\" class=\"issue-section collapsible\">\n" +
                "<h2 id=\"" + idSuffix + "\" class=\"collapsible-header\" onclick=\"toggleSection('" + idSuffix + "')\">" +
                "<span class=\"toggle-icon\">▼</span> " + StringEscapeUtils.escapeHtml4(title) + "</h2>\n" +
                "<div id=\"content-" + idSuffix + "\" class=\"collapsible-content\">\n" +
                content +
                "</div>\n" +
                "</div>\n";
    }

    private String createBackToTop() {
        return "<div class=\"back-to-top\"><a href=\"#top\">\u2191 " + translator.getInfo("back_to_top") + "</a></div>\n";
    }

    private String generateSectionFilter(String projectId, String category, List<Issue> issues) {
        StringBuilder sb = new StringBuilder();
        String idSuffix = category + "-" + projectId;

        sb.append("<div class=\"filter-container\" id=\"filter-ui-").append(idSuffix).append("\">\n");
        sb.append("<div class=\"filter-row\">\n");

        // Finder Dropdown
        Map<String, String> finderMap = issues.stream().collect(Collectors.toMap(
                Issue::getFinderName,
                i -> i.getTranslatedFinderName(translator),
                (a, b) -> a
        ));
        appendFilterGroup(sb, "finder", projectId, category, "filter_by_finder", "filter_all_finders", finderMap);

        // Sprite Dropdown
        Map<String, String> spriteMap = issues.stream()
                .map(i -> i.getActorName() != null ? i.getActorName() : "Stage")
                .distinct()
                .collect(Collectors.toMap(s -> s, s -> s));
        appendFilterGroup(sb, "sprite", projectId, category, "filter_by_sprite", "filter_all_sprites", spriteMap);

        sb.append("</div>\n</div>\n");
        return sb.toString();
    }

    // Corrected filter gen
    private void appendFilterGroup(StringBuilder sb, String type, String projectId, String category, String labelKey, String allKey, Map<String, String> options) {
        String selectId = type + "-select-" + category + "-" + projectId;
        sb.append("<div class=\"filter-group\">\n");
        sb.append("<label for=\"").append(selectId).append("\">").append(translator.getInfo(labelKey)).append("</label>\n");
        sb.append("<select id=\"").append(selectId).append("\" onchange=\"filterSection('")
                .append(category).append("', '").append(projectId).append("')\" class=\"").append(type).append("-select\">\n");
        sb.append("<option value=\"all\">").append(translator.getInfo(allKey)).append("</option>\n");
        options.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> {
            sb.append("<option value=\"").append(StringEscapeUtils.escapeHtml4(e.getKey())).append("\">")
                    .append(StringEscapeUtils.escapeHtml4(e.getValue())).append("</option>\n");
        });
        sb.append("</select>\n</div>\n");
    }

    private void renderIssue(StringBuilder sb, Issue issue) {
        String category = getCategory(issue);
        String finderVal = StringEscapeUtils.escapeHtml4(issue.getFinderName());
        String spriteName = issue.getActorName() != null && !issue.getActorName().isEmpty()
                ? StringEscapeUtils.escapeHtml4(issue.getActorName()) : "Stage";

        sb.append("<div class=\"issue\" data-type=\"").append(category)
                .append("\" data-finder=\"").append(finderVal)
                .append("\" data-sprite=\"").append(spriteName).append("\">\n");

        // Header
        sb.append("<div class=\"issue-header\">\n");
        String title = StringEscapeUtils.escapeHtml4(issue.getTranslatedFinderName(translator));
        if (issue.getActorName() != null && !issue.getActorName().isEmpty()) {
            title += " in " + translator.getInfo("sprite") + " " + spriteName;
        }
        sb.append("<span class=\"issue-title\">").append(title).append("</span>");
        sb.append("<span class=\"severity severity-").append(issue.getSeverity().name()).append("\">")
                .append(issue.getSeverity().name()).append("</span> ");
        sb.append("</div>\n");

        // Hint
        sb.append("<div class=\"hint\">\n").append(formatHint(issue.getHintText(translator))).append("</div>\n");

        // Blocks
        String scratchCode = (issue instanceof MultiBlockIssue multi)
                ? getScratchBlocksCode(issue, multi.getScriptEntities())
                : getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());

        if (scratchCode != null && !scratchCode.isEmpty()) {
            sb.append("<pre class=\"blocks\">")
                    .append(StringEscapeUtils.escapeHtml4(scratchCode.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim()))
                    .append("</pre>\n");
        }
        sb.append("</div>\n");
    }

    private String formatHint(String hint) {
        String escaped = StringEscapeUtils.escapeHtml4(hint);
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

        escaped = escaped.replaceAll("\\[sbi\\](.*?)\\[/sbi\\]", "<code class=\"scratchblocks\">$1</code>");
        escaped = escaped.replaceAll("\\[var\\](.*?)\\[/var\\]", "<code class=\"scratchblocks\">($1)</code>");
        escaped = escaped.replaceAll("\\[list\\](.*?)\\[/list\\]", "<code class=\"scratchblocks\">($1 :: list)</code>");

        return escaped;
    }

    private String replaceToken(String text, String tag, String key) {
        if (text.contains(tag)) {
            String translated = translator.getInfo(key);
            return text.replace(tag, "<code>" + StringEscapeUtils.escapeHtml4(translated) + "</code>");
        }
        return text;
    }

    private String getCategory(Issue issue) {
        return switch (issue.getIssueType()) {
            case BUG -> FinderGroup.BUGS.group();
            case SMELL -> FinderGroup.SMELLS.group();
            case PERFUME -> FinderGroup.PERFUMES.group();
            default -> FinderGroup.QUESTIONS.group();
        };
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

    private String generateGraphsContent(Program program, String projectId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"graphs-container\">\n");

        List<GraphType> types = List.of(GraphType.CFG, GraphType.CDG, GraphType.DDG, GraphType.PDG, GraphType.TDG);

        // Tabs
        sb.append("<div class=\"graph-tabs\">\n");
        boolean first = true;
        for (GraphType type : types) {
            String typeName = type.name();
            String label = getTranslatedLabel("graph_" + typeName, typeName);
            String activeClass = first ? " active" : "";
            String tabId = "graph-tab-" + typeName + "-" + projectId;
            sb.append("<div id=\"").append(tabId).append("\" class=\"graph-tab").append(activeClass)
                    .append("\" onclick=\"showGraph('").append(typeName).append("', '").append(projectId).append("')\">")
                    .append(label).append("</div>\n");
            first = false;
        }
        sb.append("</div>\n");

        // Views
        first = true;
        for (GraphType type : types) {
            String typeName = type.name();
            String activeClass = first ? " active" : "";
            sb.append("<div id=\"graph-view-").append(typeName).append("-").append(projectId)
                    .append("\" class=\"graph-view").append(activeClass)
                    .append("\"><div id=\"graphviz-").append(typeName).append("-").append(projectId)
                    .append("\" class=\"graphviz-container\"></div></div>\n");
            first = false;
        }
        sb.append("</div>\n");

        // Data Generation (Just data, no functions)
        StringBuilder jsData = new StringBuilder();
        jsData.append("window.graphData = window.graphData || {};\n");
        jsData.append("window.graphData['").append(projectId).append("'] = {};\n");

        for (GraphType type : types) {
            DotGraphAnalyzer analyzer = new DotGraphAnalyzer(type);
            String dot = styleDot(analyzer.analyze(program));
            String escapedDot = StringEscapeUtils.escapeEcmaScript(dot);
            jsData.append("window.graphData['").append(projectId).append("']['").append(type.name()).append("'] = \"")
                    .append(escapedDot).append("\";\n");
        }

        // Initial render trigger
        jsData.append("window.addEventListener('load', function() {\n");
        jsData.append("   showGraph('CFG', '").append(projectId).append("');\n");
        jsData.append("});\n");

        sb.append("<script>\n").append(jsData).append("</script>\n");

        String content = sb.toString();
        String title = getTranslatedLabel("label_graphs", "Graphs");
        return createCollapsibleSection("graphs-" + projectId, title, content);
    }

    private String generateMetricsContent(Program program, String projectId) {
        StringBuilder sb = new StringBuilder();
        String desc = getTranslatedInfo("description_metrics");
        if (desc == null) {
            desc = "Various metrics calculated for the project.";
        }

        sb.append("<p class=\"category-description\">").append(StringEscapeUtils.escapeHtml4(desc)).append("</p>\n");
        sb.append("<div class=\"metrics-container\">\n");

        ProgramMetricAnalyzer analyzer = new ProgramMetricAnalyzer();
        List<MetricResult> results = analyzer.analyze(program);
        results.sort(Comparator.comparing(MetricResult::name));

        for (MetricResult result : results) {
            String displayName = getTranslatedLabel("metric_" + result.name(), result.name());
            sb.append("<div class=\"metric-item\">\n");
            sb.append("<span class=\"metric-name\">").append(StringEscapeUtils.escapeHtml4(displayName)).append("</span>");
            sb.append("<span class=\"metric-value\">").append(String.format("%.2f", result.value())).append("</span>");
            sb.append("</div>\n");
        }
        sb.append("</div>\n").append(createBackToTop());

        String title = getTranslatedLabel("label_metrics", "Metrics");
        return createCollapsibleSection("metrics-" + projectId, title, sb.toString());
    }

    private String styleDot(String dot) {
        if (dot == null) {
            return "";
        }
        int idx = dot.indexOf("{");
        if (idx == -1) {
            return dot;
        }

        StringBuilder styled = new StringBuilder(dot.substring(0, idx + 1));
        styled.append("\n");
        styled.append("  graph [fontname=\"Segoe UI, sans-serif\", bgcolor=\"transparent\", ranksep=\"0.5\", nodesep=\"0.2\", ratio=\"0.75\"];\n");
        styled.append("  node [fontname=\"Segoe UI, sans-serif\", shape=\"box\", style=\"filled,rounded\", fillcolor=\"#ffffff\", color=\"#007bff\", penwidth=\"0\", margin=\"0.1,0.1\"];\n");
        styled.append("  edge [fontname=\"Segoe UI, sans-serif\", color=\"#6c757d\", penwidth=\"0.8\", arrowsize=\"0.8\"];\n");

        String rest = dot.substring(idx + 1);
        rest = rest.replaceAll("(label=\"(Entry|Exit)\")", "$1, penwidth=\"1.5\"");
        styled.append(rest);
        return styled.toString();
    }

    // Helpers
    private String loadResource(String path) throws IOException {
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                // For optional resources like logo, we handled them differently but for critical JS/Template this is fatal for now
                if (path.equals(LOGO_PATH)) return ""; 
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String loadLogoBase64() {
        try (var is = getClass().getResourceAsStream(LOGO_PATH)) {
            if (is != null) {
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(is.readAllBytes());
            }
        } catch (IOException e) { /* ignore */ }
        return "";
    }

    private String getTranslatedLabel(String key, String fallback) {
        String val = translator.getInfo(key);
        return (val != null && !val.startsWith("Missing key")) ? val : fallback;
    }

    private String getTranslatedInfo(String key) {
        String val = translator.getInfo(key);
        return (val != null && !val.startsWith("Missing key")) ? val : null;
    }
}
