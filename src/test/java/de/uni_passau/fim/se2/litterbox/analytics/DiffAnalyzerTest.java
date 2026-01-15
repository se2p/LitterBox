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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixHeuristicRegistry;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DiffAnalyzer}.
 */
class DiffAnalyzerTest implements JsonTest {

    private static final String BUGS_DETECTORS = "bugs";

    @Test
    void testAnalyzeSameProject_AllPersisting() throws IOException, ParsingException {
        // When comparing a project to itself, all issues should be PERSISTING
        // Note: DiffAnalyzer expects .sb3 files for comparison
        Path projectPath = Path.of("src/test/fixtures/emptyProject.sb3");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(projectPath, projectPath);

        assertNotNull(result);
        assertEquals(projectPath.toString(), result.beforeSource());
        assertEquals(projectPath.toString(), result.afterSource());

        // Same empty project means no issues
        assertTrue(result.fixed().isEmpty(), "No issues should be FIXED");
        assertTrue(result.newIssues().isEmpty(), "No issues should be NEW");
        assertTrue(result.removed().isEmpty(), "No issues should be REMOVED");
    }

    @Test
    void testAnalyzeEmptyProject() throws IOException, ParsingException {
        // Empty .sb3 project should not crash
        Path emptyProject = Path.of("src/test/fixtures/emptyProject.sb3");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(emptyProject, emptyProject);

        assertNotNull(result);
        // Empty project should have no issues
        assertTrue(result.fixed().isEmpty());
        assertTrue(result.newIssues().isEmpty());
        assertTrue(result.persisting().isEmpty());
        assertTrue(result.removed().isEmpty());
    }

    @Test
    void testDiffResultCounts() throws IOException, ParsingException {
        Path projectPath = Path.of("src/test/fixtures/emptyProject.sb3");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(projectPath, projectPath);

        // Verify count methods match list sizes
        assertEquals(result.fixed().size(), result.fixedCount());
        assertEquals(result.newIssues().size(), result.newCount());
        assertEquals(result.persisting().size(), result.persistingCount());
        assertEquals(result.removed().size(), result.removedCount());
    }

    @Test
    void testFixHeuristicRegistryInitialization() {
        // Verify all fix heuristics registered successfully
        assertTrue(FixHeuristicRegistry.allRegistrationsSucceeded(),
                "All fix heuristics should register successfully. Failed: "
                        + FixHeuristicRegistry.getFailedRegistrations());

        // Verify we have a reasonable number of heuristics registered
        assertTrue(FixHeuristicRegistry.getHeuristicCount() > 40,
                "Expected at least 40 fix heuristics to be registered, got: "
                        + FixHeuristicRegistry.getHeuristicCount());

        // Verify failed registrations list is empty
        assertTrue(FixHeuristicRegistry.getFailedRegistrations().isEmpty(),
                "No registrations should have failed");
    }

    @Test
    void testAnalyzeIgnoreLooseBlocks() throws IOException, ParsingException {
        Path projectPath = Path.of("src/test/fixtures/emptyProject.sb3");

        // Both with and without ignoring loose blocks should work
        DiffAnalyzer analyzerIgnore = new DiffAnalyzer(BUGS_DETECTORS, true, IssueTranslatorFactory.getIssueTranslator());
        DiffResult resultIgnore = analyzerIgnore.analyze(projectPath, projectPath);
        assertNotNull(resultIgnore);

        DiffAnalyzer analyzerNoIgnore = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult resultNoIgnore = analyzerNoIgnore.analyze(projectPath, projectPath);
        assertNotNull(resultNoIgnore);
    }

    @Test
    void testProgramBugAnalyzer() throws ParsingException {
        // Test the underlying ProgramBugAnalyzer directly
        Scratch3Parser parser = new Scratch3Parser();
        Program program = parser.parseFile(new File("src/test/fixtures/emptyProject.sb3"));

        ProgramBugAnalyzer analyzer = new ProgramBugAnalyzer(BUGS_DETECTORS, false);
        var issues = analyzer.analyze(program);

        assertNotNull(issues);
        // Empty project should have no issues (or very few)
    }

    @Test
    void testAnalyzeWithProjectJsonAsBefore() throws IOException, ParsingException {
        // Test that project.json files can be used as the "before" input
        Path projectJsonPath = Path.of("src/test/fixtures/emptyProject.json");
        Path sb3Path = Path.of("src/test/fixtures/emptyProject.sb3");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(projectJsonPath, sb3Path);

        assertNotNull(result);
        assertEquals(projectJsonPath.toString(), result.beforeSource());
        assertEquals(sb3Path.toString(), result.afterSource());
    }

    @Test
    void testAnalyzeWithProjectJsonAsAfter() throws IOException, ParsingException {
        // Test that project.json files can be used as the "after" input
        Path sb3Path = Path.of("src/test/fixtures/emptyProject.sb3");
        Path projectJsonPath = Path.of("src/test/fixtures/emptyProject.json");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(sb3Path, projectJsonPath);

        assertNotNull(result);
        assertEquals(sb3Path.toString(), result.beforeSource());
        assertEquals(projectJsonPath.toString(), result.afterSource());
    }

    @Test
    void testAnalyzeWithProjectJsonForBothBeforeAndAfter() throws IOException, ParsingException {
        // Test that project.json files can be used for both before and after
        Path projectJsonPath = Path.of("src/test/fixtures/emptyProject.json");

        DiffAnalyzer analyzer = new DiffAnalyzer(BUGS_DETECTORS, false, IssueTranslatorFactory.getIssueTranslator());
        DiffResult result = analyzer.analyze(projectJsonPath, projectJsonPath);

        assertNotNull(result);
        assertEquals(projectJsonPath.toString(), result.beforeSource());
        assertEquals(projectJsonPath.toString(), result.afterSource());
        // Same project should have no fixed or new issues
        assertTrue(result.fixed().isEmpty());
        assertTrue(result.newIssues().isEmpty());
    }
}
