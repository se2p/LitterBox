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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.ForeverInsideIfInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for ForeverInsideIfFix.
 * Bug: RepeatForever inside If with following statements.
 * Fix: No longer inside If, no statements follow, or forever was deleted.
 */
public class ForeverInsideIfFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NotInsideIf() throws IOException, ParsingException {
        // RepeatForever not inside an If statement - fixed
        String code = """
                when green flag clicked
                forever
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideIfFix finder = new ForeverInsideIfFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ForeverStillInsideIf() throws IOException, ParsingException {
        // RepeatForever still inside If
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                    forever
                        move (10) steps
                    end
                end
                say [Hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideIfFix finder = new ForeverInsideIfFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Bug was fixed by deleting the forever loop entirely
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        ForeverInsideIfFix finder = new ForeverInsideIfFix(createTestIssueDTO(blockId));
        // Deleting the buggy block is a valid fix
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Tests fix detection using the injector.
     *
     * <p>The ForeverInsideIfInjector wraps statements in an if-then-forever pattern.
     * The fix heuristic should detect that the original program (which doesn't have
     * the injected forever) represents a fixed state.
     */
    @Test
    public void testInjector_IfElseWithStatements() throws IOException, ParsingException {
        // Start with a fixed program - an IfElse with statements after code
        String fixedCode = """
                when green flag clicked
                if <key (space v) pressed?> then
                  move (10) steps
                else
                  turn right (15) degrees
                end
                say [done]""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no ForeverInsideIf bug
        ForeverInsideIf bugFinder = new ForeverInsideIf();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no ForeverInsideIf issue");

        // Inject bug
        ForeverInsideIfInjector injector = new ForeverInsideIfInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect ForeverInsideIf in mutated program");

        // Get the block ID of the forever loop that was added
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(
                bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect this block is fixed (deleted) in original program
        ForeverInsideIfFix fixHeuristic = new ForeverInsideIfFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

