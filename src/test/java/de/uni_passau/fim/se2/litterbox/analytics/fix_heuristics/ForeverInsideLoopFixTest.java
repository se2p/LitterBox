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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.ForeverInsideLoopInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for ForeverInsideLoopFix heuristic.
 *
 * <p>The bug is reported when a forever/until/repeat loop is nested inside another loop.
 * The fix is detected when the loop is no longer nested, or was deleted.
 */
class ForeverInsideLoopFixTest extends AbstractFixHeuristicTest {

    @Test
    void testFixFound_ForeverNotNested() throws IOException, ParsingException {
        // Fix: Forever loop is at top level (not nested inside another loop)
        String source = """
                when green flag clicked
                forever
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    void testFixFound_ForeverMovedOut() throws IOException, ParsingException {
        // Fix: Forever loop was nested, but is now at top level
        String source = """
                when green flag clicked
                repeat (10)
                    move (10) steps
                end
                forever
                    move (5) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    void testNoFix_ForeverNestedInRepeat() throws IOException, ParsingException {
        // No Fix: Forever loop is nested inside a repeat loop
        String source = """
                when green flag clicked
                repeat (10)
                    forever
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Bug was fixed by deleting the forever loop entirely
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO("non_existent_id"));
        // Deleting the buggy block is a valid fix
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // NESTED STRUCTURE TESTS
    // ========================================================================

    @Test
    void testNoFix_ForeverDeeplyNested() throws IOException, ParsingException {
        // No Fix: Forever loop deeply nested inside multiple loops
        String source = """
                when green flag clicked
                repeat (10)
                    repeat (5)
                        forever
                            move (10) steps
                        end
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    void testNoFix_ForeverInsideLoopInsideIf() throws IOException, ParsingException {
        // No Fix: Forever inside if inside loop - still nested in loop context
        String source = """
                when green flag clicked
                repeat (10)
                    if <touching (edge v)?> then
                        forever
                            move (10) steps
                        end
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    void testFixFound_ForeverInsideIfNotInLoop() throws IOException, ParsingException {
        // Fix: Forever is inside an if, but not inside a loop
        String source = """
                when green flag clicked
                if <touching (edge v)?> then
                    forever
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Tests fix detection using the injector.
     *
     * <p>The ForeverInsideLoopInjector inserts a forever loop inside another loop.
     * The fix heuristic should detect that the original program (which doesn't have
     * the injected forever inside the loop) represents a fixed state.
     */
    @Test
    void testInjector_RepeatWithStatements() throws IOException, ParsingException {
        // Start with a fixed program - repeat with statements but no forever inside
        String fixedCode = """
                when green flag clicked
                repeat (10)
                  move (10) steps
                end
                say [done]""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no ForeverInsideLoop bug
        ForeverInsideLoop bugFinder = new ForeverInsideLoop();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no ForeverInsideLoop issue");

        // Inject bug
        ForeverInsideLoopInjector injector = new ForeverInsideLoopInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect ForeverInsideLoop in mutated program");

        // Get the block ID of the forever loop that was added
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect this block is fixed (deleted) in original program
        ForeverInsideLoopFix fixHeuristic = new ForeverInsideLoopFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

