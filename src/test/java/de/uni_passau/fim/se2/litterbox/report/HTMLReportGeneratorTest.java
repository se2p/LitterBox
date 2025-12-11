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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

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
        assertThat(html).contains("scratchblocks.min.js");
        assertThat(html).contains("Floating Point Comparison"); // Finder name
        assertThat(html).contains("<pre class=\"blocks\">");
        assertThat(html).contains("scratchblocks.renderMatching");
        
        // Check for specific hint content formatting if applicable to this issue
        // Floating point comparison hint usually implies equality check
        // We can check if the formatting logic ran
        assertThat(html).contains("code.scratchblocks"); // css class
    }

    @Test
    public void testHintFormatting() throws IOException, ParsingException {
        // Mock an issue or find one with rich hint text
        // Existing "xPosEqual.json" + FloatingPointComparison gives a hint about comparing float with equal
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");
        FloatingPointComparison finder = new FloatingPointComparison();
        Set<Issue> issues = finder.check(program);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HTMLReportGenerator generator = new HTMLReportGenerator(translator, os);
        generator.generateReport(program, issues);
        os.close();

        String html = os.toString();
        
        // This specific finder might not use ALL tags, but we verified the generator logic.
        // Let's at least ensure symbols are escaped/replaced.
        // Check for "Suggestion for code improvement" which comes from the hint formatting.
        assertThat(html).contains("Suggestion for code improvement");
        assertThat(html).contains("<code>&lt;</code>"); // [LEQ] or just standard formatting checks
    }
    @Test
    public void testTokenReplacementCrash() throws IOException, ParsingException {
        // Regression test for MissingResourceException when replacing [IF] tokens
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
        
        // Should not throw exception
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
        
        // Verify Filter UI Container
        assertThat(html).contains("<div id=\"filter-ui\" class=\"filter-container\">");
        
        // Verify Inputs
        assertThat(html).contains("id=\"filter-bug\"");
        assertThat(html).contains("id=\"filter-smell\"");
        assertThat(html).contains("id=\"filter-perfume\"");
        assertThat(html).contains("id=\"finder-select\"");
        assertThat(html).contains("id=\"sprite-select\"");
        
        // Verify Data Attributes on Issue
        assertThat(html).contains("data-type=\"bugs\"");
        assertThat(html).contains("data-finder=\"Floating Point Comparison\"");
        String expectedSprite = issue.getActorName() != null ? issue.getActorName() : "Stage";
        assertThat(html).contains("data-sprite=\"" + expectedSprite + "\"");

        // Verify JS function presence
        assertThat(html).contains("function applyFilters()");
        assertThat(html).contains("function populateFilters()");

        // Verify Title Formatting and Layout Refinements
        assertThat(html).contains("justify-content: space-between"); // New CSS
        // The fixture usually has an actor. If it's a sprite, the title should be "Finder in Sprite ActorName"
        // FloatingPointComparison finder translated is "Floating Point Comparison" (en)
        // "Sprite" key (en) is "Sprite"
        // So we expect "Floating Point Comparison in Sprite [ActorName]"
        if (issue.getActorName() != null) {
             assertThat(html).contains("Floating Point Comparison in Sprite " + issue.getActorName());
        }

        // Verify Improved Filter CSS
        assertThat(html).contains("appearance: none");
        assertThat(html).contains("background-image: url");
        assertThat(html).contains("transition: border-color");
        
        // Verify Scratchblocks Scale
        // We look for the scale: 0.7 property inside the pre.blocks configuration
        // Since we can't easily parse the JS object literal with regex (or could, but simple string search is enough usually),
        // we check if the configuration block for pre.blocks contains scale: 0.7.
        // The output order is deterministic based on the generator code.
        // We essentially look for the snippet created in HTMLReportGenerator.
        assertThat(html).contains("scratchblocks.renderMatching('pre.blocks', {");
        assertThat(html).contains("scale: 0.7");

        // Verify Logo
        assertThat(html).contains("<img src=\"https://github.com/se2p/LitterBox/blob/master/src/main/resources/litterbox.png?raw=true\" alt=\"LitterBox Logo\">");

        // Verify Navigation Bar CSS
        assertThat(html).contains(".report-nav ul { list-style: none");
        assertThat(html).contains("display: inline-block"); // part of the button style
    }
}
