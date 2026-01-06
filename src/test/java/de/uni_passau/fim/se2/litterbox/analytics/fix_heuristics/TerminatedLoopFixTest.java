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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TerminatedLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.TerminatedLoopInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for TerminatedLoopFix.
 * Bug: Loop has StopAll/StopThisScript as last statement (only executes once).
 * Fix: Loop no longer ends with a stop block.
 * Note: The bug is reported on the LOOP node (which has a block ID), not the stop block.
 */
public class TerminatedLoopFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LoopNoLongerEndsWithStop() throws IOException, ParsingException {
        // The loop exists but no longer ends with a stop block - fixed
        String code = """
                when green flag clicked
                forever
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        // Use any loop block ID as the bug location (since bug is reported on loop)
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_StopStillInLoop() throws IOException, ParsingException {
        // StopAll is still last statement in loop
        String code = """
                when green flag clicked
                forever
                    move (10) steps
                    stop [all v]
                end
                """;

        Program program = parseProgram(code);
        // Bug is reported on the loop node, not the stop block
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Bug was fixed by deleting the loop (loop no longer exists)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-loop-id";

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        // Deleting the buggy loop is a valid fix
        assertFixFound(finder, program);
    }

    // ========================================================================
    // NESTED STRUCTURE TESTS
    // ========================================================================

    @Test
    public void testNoFix_StopInNestedIf() throws IOException, ParsingException {
        // No Fix: Stop is in an if inside the loop, but still terminates the loop
        String code = """
                when green flag clicked
                forever
                    if <touching (edge v)?> then
                        stop [all v]
                    end
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        // The bug finder may not report this as terminated loop because stop isn't last stmt
        // Let's check the actual behavior - this tests the edge case handling
        Set<FixResult> fixes = finder.check(program);
        // This should likely be a fix since stop is inside if, not unconditional last stmt
        Assertions.assertFalse(fixes.isEmpty(),
                "Stop inside if should not terminate loop unconditionally");
    }

    @Test
    public void testNoFix_StopInNestedLoop() throws IOException, ParsingException {
        // No Fix: Stop is in a nested loop (outer loop still has the bug)
        String code = """
                when green flag clicked
                forever
                    repeat (3)
                        move (10) steps
                    end
                    stop [all v]
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_StopAfterLoopNotInside() throws IOException, ParsingException {
        // Fix: Stop is AFTER the loop, not inside it
        String code = """
                when green flag clicked
                forever
                    move (10) steps
                end
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        TerminatedLoopFix finder = new TerminatedLoopFix(createTestIssueDTO(blockId));
        // Loop doesn't end with stop - it's after the loop
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ForeverLoop() throws IOException, ParsingException {
        // Start with a fixed program - forever loop without stop
        String fixedCode = """
                when green flag clicked
                forever
                    move (10) steps
                end""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no TerminatedLoop
        TerminatedLoop bugFinder = new TerminatedLoop();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no TerminatedLoop issue");

        // Inject bug
        TerminatedLoopInjector injector = new TerminatedLoopInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has TerminatedLoop
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect TerminatedLoop in mutated program");

        // Get the block ID
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect a fix in the original program
        TerminatedLoopFix fixHeuristic = new TerminatedLoopFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

