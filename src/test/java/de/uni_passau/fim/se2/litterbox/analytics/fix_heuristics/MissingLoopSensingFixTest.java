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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingLoopSensingInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MissingLoopSensingFix.
 * Bug: Sensing blocks used outside a loop.
 * Fix: Sensing block is now inside a loop or after wait/ask.
 */
public class MissingLoopSensingFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_InsideLoop() throws IOException, ParsingException {
        // Key pressed inside forever loop - fix reported
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingFix finder = new MissingLoopSensingFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_AfterWaitUntil() throws IOException, ParsingException {
        // Sensing after wait until - fix reported
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                if <key (space v) pressed?> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        // Find the second IsKeyPressed (the one in the if)
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingFix finder = new MissingLoopSensingFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_InsideWaitUntilCondition() throws IOException, ParsingException {
        // Sensing block used AS the condition for WaitUntil - fix reported
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingFix finder = new MissingLoopSensingFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Fix: Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingLoopSensingFix finder = new MissingLoopSensingFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // These tests use the injector to create bugs, then verify the fix heuristic
    // correctly identifies the original program as having fixed those bugs.
    // ========================================================================

    /**
     * Tests fix detection using the injector's first test case.
     *
     * <p>Workflow:
     * <ol>
     *   <li>Start with a "fixed" program (sensing inside forever loop)</li>
     *   <li>Inject a bug (removes the loop)</li>
     *   <li>Run bug finder on mutated program to get the buggy block ID</li>
     *   <li>Run fix heuristic with that block ID on the original program</li>
     *   <li>Assert that the fix is correctly detected</li>
     * </ol>
     */
    @Test
    public void testInjector_SimpleLoopWithSensingCondition() throws IOException, ParsingException {
        // Same code as in MissingLoopSensingInjectorTest.testMissingLoopSensingInjector
        String fixedCode = """
                when green flag clicked
                forever
                    if <key [space] pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingLoopSensing bugFinder = new MissingLoopSensing();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingLoopSensing issue");

        // Inject bug
        MissingLoopSensingInjector injector = new MissingLoopSensingInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingLoopSensing in mutated program");

        // Get the sensing block ID from the ORIGINAL program
        // (CloneVisitor regenerates block IDs, so mutated program has different IDs)
        String sensingBlockId = findBlockId(fixedProgram, IsKeyPressed.class);
        Assertions.assertNotNull(sensingBlockId,
                "Should find IsKeyPressed block in original program");

        // The fix heuristic should detect this block is in a fixed state
        MissingLoopSensingFix fixHeuristic = new MissingLoopSensingFix(createTestIssueDTO(sensingBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }

    /**
     * Tests fix detection with multiple sensing conditions in the same loop.
     *
     * <p>Uses the injector's second test case with two if-statements containing
     * sensing conditions inside the same forever loop.
     */
    @Test
    public void testInjector_MultipleIfConditionsInLoop() throws IOException, ParsingException {
        // Program with TWO sensing conditions in the same forever loop
        String fixedCode = """
                when green flag clicked
                forever
                    if <key [space] pressed?> then
                        move (10) steps
                    end
                    if <key [a v] pressed?> then
                        turn right (180) degrees
                    end
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingLoopSensing bugFinder = new MissingLoopSensing();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingLoopSensing issue");

        // Inject bug
        MissingLoopSensingInjector injector = new MissingLoopSensingInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingLoopSensing in mutated program");

        // Find the FIRST IsKeyPressed block in original program
        // (both sensing conditions should be inside the loop, so the fix should be detected)
        String sensingBlockId = findBlockId(fixedProgram, IsKeyPressed.class);
        Assertions.assertNotNull(sensingBlockId,
                "Should find IsKeyPressed block in original program");

        // The fix heuristic should detect this block is in a fixed state
        MissingLoopSensingFix fixHeuristic = new MissingLoopSensingFix(createTestIssueDTO(sensingBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
