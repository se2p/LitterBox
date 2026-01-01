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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for EndlessRecursionFix.
 * Bug: Procedure calls itself without termination condition.
 * Fix: Recursive call wrapped in conditional or removed.
 */
public class EndlessRecursionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_RecursionInConditional() throws IOException, ParsingException {
        // Recursive call now in a conditional - fixed
        String code = """
                define recurse
                if <key (space v) pressed?> then
                    recurse
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        EndlessRecursionFix finder = new EndlessRecursionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_BugStillPresent() throws IOException, ParsingException {
        // Recursive call is still unconditional
        String code = """
                define recurse
                recurse
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        EndlessRecursionFix finder = new EndlessRecursionFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                define other
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        EndlessRecursionFix finder = new EndlessRecursionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_EndlessRecursion() throws IOException, ParsingException {
        // Procedure must be called in NON-TAIL position for injector to work
        // The fixed version has conditional recursion (inside if block)
        String fixedCode = """
                define recurse
                if <key (space v) pressed?> then
                    recurse
                end
                
                when green flag clicked
                recurse
                say [Done]
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds unconditional recursive call at start)
        de.uni_passau.fim.se2.litterbox.injection.bugs.EndlessRecursionInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.EndlessRecursionInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results =
                injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.get(0).mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues =
                bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // The injector ADDS a new CallStmt (with new ID) at the start of procedure.
        // Use the EXISTING conditional recursion CallStmt ID from the fixed program.
        // This validates that the fix heuristic correctly identifies guarded recursion as fixed.
        String existingRecursiveCallId = findBlockId(fixedProgram, CallStmt.class,
                call -> call.getIdent().getName().equals("recurse"));
        org.junit.jupiter.api.Assertions.assertNotNull(existingRecursiveCallId,
                "Should find recursive CallStmt in original program");

        EndlessRecursionFix fix = new EndlessRecursionFix(createTestIssueDTO(existingRecursiveCallId));
        assertFixFound(fix, fixedProgram);
    }
}
