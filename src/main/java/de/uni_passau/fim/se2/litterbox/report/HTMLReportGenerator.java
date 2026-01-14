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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates an HTML report for the analysis results.
 */
public class HTMLReportGenerator implements ReportGenerator {

    // private static final TemplateEngine templateEngine = TemplateEngine.create(
    //         new ResourceCodeResolver(""), ContentType.Html
    // );
    // The one above only for local development to support quick reloading when the template changes.
    // We cannot use it for the final JAR, since otherwise a JDK would be required when running LitterBox.
    private static final TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

    private static final Escaper HTML_ESCAPER = HtmlEscapers.htmlEscaper();

    private final IssueTranslator translator;
    private final OutputStream outputStream;

    private static final String LOGO_PATH = "/litterbox.svg";
    private static final String FAVICON_PATH = "/favicon.ico";

    private static final String PATH_SCRATCHBLOCKS_JS = "/js/scratchblocks.min.js";
    private static final String PATH_TRANSLATIONS_JS = "/js/translations-all.js";
    private static final String PATH_VIZ_JS = "/js/viz-global.js";
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
        final String logo = loadResource(LOGO_PATH);
        final String faviconBase64 = loadFaviconBase64();
        final Scripts scripts = buildScripts();
        final List<ProjectAnalysisResult> sortedProjects = sortProjects(projects);
        final List<ProjectModel> projectModels = buildProjectModels(sortedProjects);

        try (var pw = new PrintWriter(outputStream)) {
            final TemplateOutput output = new PrintWriterOutput(pw);
            final TemplateModel model = new TemplateModel(
                    translator.getLocale().getLanguage(),
                    faviconBase64,
                    logo,
                    scripts,
                    translator,
                    projectModels
            );
            templateEngine.render("report_template.jte", model, output);
        }
    }

    private Scripts buildScripts() throws IOException {
        final String scratchblocksJs = loadResource(PATH_SCRATCHBLOCKS_JS);
        final String translationsJs = loadResource(PATH_TRANSLATIONS_JS);
        final String vizJs = loadResource(PATH_VIZ_JS);
        final String svgPanZoomJs = loadResource(PATH_SVG_PAN_ZOOM_JS);

        return new Scripts(
                scratchblocksJs,
                translationsJs,
                vizJs,
                svgPanZoomJs
        );
    }

    private List<ProjectModel> buildProjectModels(final List<ProjectAnalysisResult> projects) {
        final List<ProjectModel> projectModels = new ArrayList<>();

        for (ProjectAnalysisResult project : projects) {
            final String projectId = generateProjectId(project.projectName());
            final List<IssueGroup> issues = buildIssues(project.issues());
            final List<Graph> graphs = buildGraphs(project.program());
            final List<Stat> stats = buildStats(project.program(), project.issues());
            final List<Metric> metrics = buildMetrics(project.program());

            projectModels.add(new ProjectModel(
                    projectId,
                    project.projectName(),
                    issues,
                    graphs,
                    stats,
                    metrics
            ));
        }

        return projectModels;
    }

    private List<ProjectAnalysisResult> sortProjects(Collection<ProjectAnalysisResult> projects) {
        List<ProjectAnalysisResult> sorted = new ArrayList<>(projects);
        sorted.sort(Comparator.comparing(ProjectAnalysisResult::projectName));
        return sorted;
    }

    private String generateProjectId(String projectName) {
        return "project-" + projectName.replaceAll("[^a-zA-Z0-9]", "-");
    }

    private List<Stat> buildStats(final Program program, final Collection<Issue> issues) {
        final List<Stat> result = new ArrayList<>();

        final int spriteCount = (int) new SpriteCount<>().calculateMetric(program);
        final int scriptCount = (int) new ScriptCount<>().calculateMetric(program);
        final int blockCount = (int) new BlockCount<>().calculateMetric(program);

        final long bugCount = issues.stream().filter(i -> i.getIssueType() == IssueType.BUG).count();
        final long smellCount = issues.stream().filter(i -> i.getIssueType() == IssueType.SMELL).count();
        final long perfumeCount = issues.stream().filter(i -> i.getIssueType() == IssueType.PERFUME).count();

        final double ctScore = new ComputationalThinkingScore().calculateMetric(program);

        result.add(new Stat(translator.getInfo("stats_sprites"), Integer.toString(spriteCount)));
        result.add(new Stat(translator.getInfo("stats_scripts"), Integer.toString(scriptCount)));
        result.add(new Stat(translator.getInfo("stats_blocks"), Integer.toString(blockCount)));
        result.add(new Stat(translator.getInfo("label_bugs"), Long.toString(bugCount)));
        result.add(new Stat(translator.getInfo("label_smells"), Long.toString(smellCount)));
        result.add(new Stat(translator.getInfo("label_perfumes"), Long.toString(perfumeCount)));
        result.add(new Stat(translator.getInfo("stats_ct_score"), String.format("%.1f", ctScore)));

        return result;
    }

    private List<IssueGroup> buildIssues(final Collection<Issue> issues) {
        final List<IssueGroup> issueGroups = new ArrayList<>();

        final Set<IssueType> relevantIssueTypes = Set.of(IssueType.PERFUME, IssueType.BUG, IssueType.SMELL);
        final Map<String, List<Issue>> issuesByCategory = issues.stream()
                .filter(issue -> relevantIssueTypes.contains(issue.getIssueType()))
                .collect(Collectors.groupingBy(this::getCategory));

        for (final var entry : issuesByCategory.entrySet()) {
            final Map<String, List<RenderedIssue>> renderedIssuesByFinder = new HashMap<>();
            final Map<String, List<Issue>> issuesByFinder = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(Issue::getFinderName));

            for (final var byFinderEntry : issuesByFinder.entrySet()) {
                final List<RenderedIssue> issuesForFinder = byFinderEntry.getValue()
                        .stream()
                        .map(this::convertIssue)
                        .toList();
                renderedIssuesByFinder.put(byFinderEntry.getKey(), issuesForFinder);
            }

            final Map<String, String> finders = entry.getValue().stream().collect(Collectors.toMap(
                    Issue::getFinderName,
                    i -> i.getTranslatedFinderName(translator),
                    (a, b) -> a
            ));
            final List<String> sprites = entry.getValue().stream()
                    .map(i -> i.getActorName() != null ? i.getActorName() : "Stage")
                    .distinct()
                    .sorted()
                    .toList();

            issueGroups.add(new IssueGroup(entry.getKey(), renderedIssuesByFinder, finders, sprites));
        }

        return issueGroups;
    }

    private RenderedIssue convertIssue(final Issue issue) {
        String sb = getIssueScratchBlocks(issue);
        if (sb != null) {
            sb = sb.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
        }

        String refactoredScript = getScratchBlocksCode(issue, issue.getRefactoredScriptOrProcedureDefinition());
        if (refactoredScript != null) {
            refactoredScript = refactoredScript.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
        }

        return new RenderedIssue(
                issue.getId(),
                getCategory(issue),
                issue.getFinderName(),
                issue.getTranslatedFinderName(translator),
                issue.getActorName(),
                formatHint(issue.getHintText(translator)),
                sb,
                refactoredScript,
                getSeverityClass(issue),
                issue.getSeverity().name(),
                getSeverityIcon(issue)
        );
    }

    private String getIssueScratchBlocks(final Issue issue) {
        return (issue instanceof MultiBlockIssue multi)
                ? getScratchBlocksCode(issue, multi.getScriptEntities())
                : getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());
    }

    private String formatHint(String hint) {
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

    private List<Graph> buildGraphs(final Program program) {
        final List<GraphType> types = List.of(
                GraphType.CFG, GraphType.CDG, GraphType.DDG, GraphType.PDG, GraphType.TDG
        );
        final List<Graph> graphs = new ArrayList<>();

        for (final GraphType type : types) {
            final DotGraphAnalyzer analyzer = new DotGraphAnalyzer(type);
            final String dot = styleDot(analyzer.analyze(program));
            graphs.add(new Graph(type.toString(), dot));
        }

        return graphs;
    }

    private List<Metric> buildMetrics(final Program program) {
        final List<Metric> result = new ArrayList<>();

        ProgramMetricAnalyzer analyzer = new ProgramMetricAnalyzer();
        List<MetricResult> metrics = analyzer.analyze(program);
        metrics.sort(Comparator.comparing(MetricResult::name));

        for (MetricResult metric : metrics) {
            result.add(new Metric(translator.getInfo("metric_" + metric.name()), metric.value()));
        }

        return result;
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
        styled.append("""
                graph [
                    fontname="sans-serif",
                    bgcolor="transparent",
                    ranksep="0.5",
                    nodesep="0.2",
                    ratio="0.75"
                ];
                """);
        styled.append("""
                node [
                    fontname="sans-serif",
                    shape="box",
                    style="filled,rounded",
                    fillcolor="#ffffff",
                    color="#007bff",
                    penwidth="0",
                    margin="0.1,0.1"
                ];
                """);
        styled.append("""
                edge [
                    fontname="sans-serif",
                    color="#6c757d",
                    penwidth="0.8",
                    arrowsize="0.8"
                ];
                """);

        String rest = dot.substring(idx + 1);
        rest = rest.replaceAll("(label=\"(Entry|Exit)\")", "$1, penwidth=\"1.5\"");
        styled.append(rest);
        return styled.toString();
    }

    // Helpers
    private String loadResource(String path) throws IOException {
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                // For optional resources like logo, we handled them differently,
                // but for critical JS/Template this is fatal for now
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
            return  "severity-" + issue.getSeverity().name();
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

    public record TemplateModel(
            String language,
            String favicon,
            String logo,
            Scripts scripts,
            IssueTranslator translator,
            List<ProjectModel> projects
    ) {}

    public record Scripts(
            String scratchBlocks,
            String scratchBlocksTranslations,
            String graphviz,
            String svgPanZoom
    ) {}

    public record ProjectModel(
            String id,
            String name,
            List<IssueGroup> issues,
            List<Graph> graphs,
            List<Stat> stats,
            List<Metric> metrics
    ) {}

    public record Graph(String type, String dotGraph) {}

    public record Stat(String label, String value) {}

    public record Metric(String name, double value) {}

    public record IssueGroup(
            String category,
            Map<String, List<RenderedIssue>> issues,
            Map<String, String> finders,
            List<String> sprites
    ) {}

    public record RenderedIssue(
            int id,
            String category,
            String finder,
            String translatedFinderName,
            String sprite,
            String hintText,
            @Nullable String scratchBlocks,
            @Nullable String refactoredScript,
            String severityClass,
            String severity,
            String severityIcon
    ) {}
}
