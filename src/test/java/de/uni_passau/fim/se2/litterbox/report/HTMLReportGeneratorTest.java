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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;

public class HTMLReportGeneratorTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testSingleIssueHTMLStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        FloatingPointComparison finder = new FloatingPointComparison();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();

        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<title>LitterBox Report</title>");
        assertThat(html).contains("scratchblocks"); // Library name
        assertThat(html).contains("scratchblocks v"); // Version header in embedded file
        assertThat(html).contains("Floating Point Comparison"); // Finder name
        assertThat(html).contains("<span class=\"stat-label\">Sprites</span>"); // Check stat label
        assertThat(html).contains("<pre class=\"blocks\">");

        // for single projects we do not have a navigation sidebar
        assertThat(html).contains("class=\"sidebar boxed\" style=\"display: none;\"");

        // Should contain the new header
        assertThat(html).contains("<h1>Project ");
        assertThat(html).contains("id=\"project-name-header\"");

        // Logo check
        assertThat(html).doesNotContain("{{LOGO}}");
        assertThat(html).contains("data:image/png;base64");
        assertThat(html).contains("class=\"logo\"");
    }

    @Test
    public void testHintFormatting() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        FloatingPointComparison finder = new FloatingPointComparison();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();

        assertThat(html).contains("Suggestion for code improvement");
        assertThat(html).contains("<code>&lt;</code>");
    }

    @Test
    public void testTokenReplacementCrash() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        ActorDefinition actor = program.getActorDefinitionList().getDefinitions().stream().findFirst().orElseThrow();

        FloatingPointComparison finder = new FloatingPointComparison();

        Issue issue = new IssueBuilder()
                .withFinder(finder)
                .withSeverity(IssueSeverity.MEDIUM)
                .withProgram(program)
                .withActor(actor)
                .withHint(Hint.fromText("Check the [IF] condition and [TRUE] value."))
                .build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);

        generator.generateReport(program, Collections.singleton(issue));

        String html = os.toString();
        assertThat(html).contains("<code>if</code>");
        assertThat(html).contains("<code>TRUE</code>");
    }

    @Test
    public void testFilterUIStructure() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        FloatingPointComparison finder = new FloatingPointComparison();
        Set<Issue> issues = finder.check(program);
        Issue issue = issues.iterator().next();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();

        // Verify Filter UI Container with ID suffix
        // Default project name "Project" -> ID "project-Project"
        // We now have filters per section, e.g. bugs
        assertThat(html).contains("id=\"filter-ui-bugs-project-Project\"");

        // Verify Inputs with ID suffix
        assertThat(html).contains("id=\"finder-select-bugs-project-Project\"");
        assertThat(html).contains("id=\"sprite-select-bugs-project-Project\"");

        // Verify JS calls
        assertThat(html).contains("filterSection('bugs', 'project-Project')");
    }

    @Test
    public void testMultiProjectReport() throws IOException, ParsingException {
        Program program1 = getAST("src/test/fixtures/bugs/xPosEqual.json");
        FloatingPointComparison finder = new FloatingPointComparison();
        Set<Issue> issues1 = finder.check(program1);

        Program program2 = getAST("src/test/fixtures/bugs/xPosEqual.json"); // Reusing same fixture for simplicity
        Set<Issue> issues2 = Collections.emptySet(); // Empty issues for second project

        List<ProjectAnalysisResult> projects = new ArrayList<>();
        projects.add(new ProjectAnalysisResult("ProjectA", program1, issues1));
        projects.add(new ProjectAnalysisResult("ProjectB", program2, issues2));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(projects);
        os.close();

        String html = os.toString();

        // For multi project, we DO expect sidebar
        assertThat(html).doesNotContain("class=\"single-project\"");
        assertThat(html).contains("class=\"sidebar");

        // Check sidebar contains both projects
        assertThat(html).contains("ProjectA");
        assertThat(html).contains("ProjectB");
        assertThat(html).contains("onclick=\"selectProject('project-ProjectA'");
        assertThat(html).contains("onclick=\"selectProject('project-ProjectB'");

        // Check content sections exist
        assertThat(html).contains("id=\"project-template-project-ProjectA\"");
        assertThat(html).contains("id=\"project-template-project-ProjectB\"");

        // Check Header NOT present
        assertThat(html).doesNotContain("class=\"report-header\"");
        assertThat(html).doesNotContain("Code Report for");

        // Check Project Titles ARE present
        assertThat(html).contains("id=\"project-name-header\"");

        // Check issues for ProjectA are present
        // We now use internal name for data-finder, e.g. "FloatingPointComparison" (class simple name or name field)

        // Check finding name for FloatingPointComparison
        assertThat(html).contains("data-finder=\"floating_point_comparison\"");

        // Check that the dropdown option has internal name as value and translated name as text
        // "Floating Point Comparison" is the translated name for English
        assertThat(html).contains("<option value=\"floating_point_comparison\">Floating Point Comparison</option>");

        // Check empty message for ProjectB
        assertThat(html).contains("No issues found!");
    }

    @Test
    public void testMetricsSectionPresent() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        Set<Issue> issues = Collections.emptySet();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();

        assertThat(html).contains("id=\"section-metrics-project-Project\"");
        assertThat(html).contains("class=\"metrics-container\"");
        assertThat(html).contains("class=\"metric-item\"");
        assertThat(html).contains("class=\"metric-name\"");
        assertThat(html).contains("class=\"metric-value\"");
        // Check for a specific metric likely to be present
        // We only check for the container as metrics might be empty in test environment depending on configuration
        assertThat(html).contains("class=\"metrics-container\"");
    }

    @Test
    public void testGraphsSectionPresent() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        Set<Issue> issues = Collections.emptySet();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();

        assertThat(html).contains("id=\"section-graphs-project-Project\"");
        assertThat(html).contains("class=\"graphs-container");
        // Check for scripts being included (embedded)
        assertThat(html).contains("Viz.js");
        assertThat(html).contains("svg-pan-zoom");

        // Check for tabs
        assertThat(html).contains("class=\"graph-tab\"");
        assertThat(html).contains("onclick=\"showGraph('CFG', 'project-Project')\"");
        // Check for graphviz container
        assertThat(html).contains("id=\"graphviz-CFG-project-Project\"");
        // Check for JS data injection
        assertThat(html).contains("window.graphData['project-Project'] = {");
        assertThat(html).contains("'CFG': '");
        // Check for render call
        assertThat(html).contains("Viz.instance().then(viz =>");
        assertThat(html).contains("typeof svgPanZoom !== 'undefined'");
    }
}
