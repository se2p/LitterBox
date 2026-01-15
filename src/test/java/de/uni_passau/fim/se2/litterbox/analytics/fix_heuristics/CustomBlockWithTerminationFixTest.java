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
 * Tests for CustomBlockWithTerminationFix.
 * Bug: Custom block contains StopAll/StopThisScript (terminates unexpectedly).
 * Fix: Stop statement removed or in conditional.
 */
public class CustomBlockWithTerminationFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoStopInProcedure() throws IOException, ParsingException {
        // Procedure no longer contains stop statement - fixed
        String code = """
                define myProc
                move (10) steps
                turn cw (15) degrees
                
                when green flag clicked
                myProc
                say [Done!]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CustomBlockWithTerminationFix finder = new CustomBlockWithTerminationFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_StopStillInProcedure() throws IOException, ParsingException {
        // Procedure still contains stop statement
        String code = """
                define myProc
                stop [all v]
                
                when green flag clicked
                myProc
                say [Done!]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CustomBlockWithTerminationFix finder = new CustomBlockWithTerminationFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the call is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        CustomBlockWithTerminationFix finder = new CustomBlockWithTerminationFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_CustomBlockWithTermination() throws IOException, ParsingException {
        // Start with a valid program (procedure without termination)
        String fixedCode = """
                define myProc
                move (10) steps
                turn cw (15) degrees
                
                when green flag clicked
                myProc
                say [Done!]
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds StopAll to procedure)
        de.uni_passau.fim.se2.litterbox.injection.bugs.CustomBlockWithTerminationInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.CustomBlockWithTerminationInjector();
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

        // Fix check: Get bug location and verify fix heuristic identifies original as fixed
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil
                .getBlockId(bugIssues.iterator().next().getCodeLocation());

        CustomBlockWithTerminationFix fix = new CustomBlockWithTerminationFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
