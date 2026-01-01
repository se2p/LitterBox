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
 * Tests for CustomBlockWithForeverFix.
 * Bug: Custom block contains a forever loop (never returns).
 * Fix: Forever loop removed from custom block, or is last statement.
 */
public class CustomBlockWithForeverFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoForeverInProcedure() throws IOException, ParsingException {
        // Procedure no longer contains forever loop - fixed
        String code = """
                define myProc
                move (10) steps
                turn cw (15) degrees
                
                when green flag clicked
                myProc
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CustomBlockWithForeverFix finder = new CustomBlockWithForeverFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ForeverStillInProcedure() throws IOException, ParsingException {
        // Procedure still contains forever loop
        String code = """
                define myProc
                forever
                    move (10) steps
                end
                
                when green flag clicked
                myProc
                say [Hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CustomBlockWithForeverFix finder = new CustomBlockWithForeverFix(createTestIssueDTO(blockId));
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

        CustomBlockWithForeverFix finder = new CustomBlockWithForeverFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testInjector_CustomBlockWithForever() throws IOException, ParsingException {
        // Start with a valid program (loop is fine if it's not blocking a subsequent call, 
        // or loop is not in custom block. Injector usually puts forever loop into custom block)
        // Let's use a simple proc.
        String fixedCode = """
                define myProc
                move (10) steps
                
                when green flag clicked
                myProc
                say [Done]
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Add forever loop to myProc
        de.uni_passau.fim.se2.litterbox.injection.bugs.CustomBlockWithForeverInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.CustomBlockWithForeverInjector();

        Program buggyProgram = injector.inject(fixedProgram).get(0).mutatedProgram();

        // Verify the bug is found
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> issues =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithForever().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program has no loop)
        de.uni_passau.fim.se2.litterbox.analytics.Issue issue = issues.iterator().next();
        String bugID = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        CustomBlockWithForeverFix heuristic = new CustomBlockWithForeverFix(createTestIssueDTO(bugID));
        assertFixFound(heuristic, fixedProgram);
    }
}
