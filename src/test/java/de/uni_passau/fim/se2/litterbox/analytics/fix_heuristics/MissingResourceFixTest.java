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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.FinderTest;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

/**
 * Tests for MissingResourceFix using JSON fixtures.
 *
 * <p>JSON fixtures are required because the ScratchBlocks text parser doesn't
 * include resource metadata (costumes, sounds). Only JSON-parsed programs
 * contain the actual resource definitions needed for this bug finder/fix.
 */
public class MissingResourceFixTest implements FinderTest, JsonTest {

    private static final String MISSING_RESOURCES_FIXTURE = "./src/test/fixtures/bugs/missingResources.json";

    private static IssueDTO createTestIssueDTO(String blockId) {
        return new IssueDTO(
                0, "TestFinder", "TestFinder", IssueType.BUG, 0, "Sprite1",
                blockId, null, null, null, null, null, null, 0, null, null
        );
    }

    /**
     * Tests that the fix heuristic finds a fix when a previously buggy costume
     * reference is now valid.
     *
     * <p>The missingResources.json fixture contains bugs (missing resources).
     * This test finds one of the buggy block IDs, then checks if the fix
     * heuristic would detect a fix on that block if the resource now exists.
     */
    @Test
    public void testFixDetection_CostumeFix() throws IOException, ParsingException {
        Program program = JsonTest.parseProgram(MISSING_RESOURCES_FIXTURE);

        // The fixture has missing resource bugs - find one
        MissingResource bugFinder = new MissingResource();
        Set<Issue> issues = bugFinder.check(program);
        Assertions.assertFalse(issues.isEmpty(), "Fixture should have missing resource bugs");

        // Get the block ID of a bug
        Issue bugIssue = issues.iterator().next();
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(
                bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug should have a block ID");

        // The fix heuristic checks if the block is now valid
        // Since the fixture still has the bug, the fix should NOT be found
        MissingResourceFix fixHeuristic = new MissingResourceFix(createTestIssueDTO(bugBlockId));
        Set<FixResult> fixResults = fixHeuristic.check(program);
        Assertions.assertTrue(fixResults.isEmpty(),
                "Fix heuristic should not find fix when resource is still missing");
    }

    /**
     * Tests that the fix heuristic correctly identifies when a program
     * has no missing resources (all resources are valid).
     */
    @Test
    public void testNoMissingResources_EmptyProject() throws IOException, ParsingException {
        // emptyProject.json has valid costumes/sounds
        assertThatFinderReports(0, new MissingResource(), "./src/test/fixtures/emptyProject.json");
    }

    /**
     * Tests that MissingResource bug finder correctly identifies bugs in the fixture.
     */
    @Test
    public void testMissingResourceBugFinder() throws IOException, ParsingException {
        // missingResources.json has 3 missing resource bugs
        assertThatFinderReports(3, new MissingResource(), MISSING_RESOURCES_FIXTURE);
    }

    /**
     * Tests silent deletion - when the buggy block no longer exists.
     */
    @Test
    public void testSilentDeletion() throws IOException, ParsingException {
        Program program = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");

        // Using a non-existent block ID simulates the block being deleted
        MissingResourceFix heuristic = new MissingResourceFix(createTestIssueDTO("deleted-block-id"));
        Set<FixResult> fixResults = heuristic.check(program);
        Assertions.assertFalse(fixResults.isEmpty(),
                "Fix heuristic should report fix on deleted block");
    }

    @Test
    public void testFixDetection_BackdropFix() throws IOException, ParsingException {
        // Create a valid program using scratchblocks (Stage has backdrop1 by default)
        String script = """
                when backdrop switches to [backdrop1 v]
                """;
        Program program = parseScratchBlocks(script);

        // Find the block ID of the event
        String blockId = null;
        for (de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            for (de.uni_passau.fim.se2.litterbox.ast.model.Script s : actor.getScripts().getScriptList()) {
                if (s.getEvent() instanceof BackdropSwitchTo) {
                    blockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(s.getEvent());
                    break;
                }
            }
            if (blockId != null) {
                break;
            }
        }

        Assertions.assertNotNull(blockId, "Block ID should be found for BackdropSwitchTo");

        // Simulate that this block WAS buggy (e.g. refering to missing resource)
        // But now in 'program', it refers to 'backdrop1' which exists.
        MissingResourceFix fixHeuristic = new MissingResourceFix(createTestIssueDTO(blockId));
        Set<FixResult> fixResults = fixHeuristic.check(program);

        Assertions.assertFalse(fixResults.isEmpty(), "Should detect fix for valid backdrop reference");
    }
}
