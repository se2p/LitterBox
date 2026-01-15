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
 * Tests for CallWithoutDefinitionFix.
 * Bug: CallStmt references a procedure that doesn't exist.
 * Fix: Procedure definition now exists.
 */
public class CallWithoutDefinitionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ProcedureNowDefined() throws IOException, ParsingException {
        // Call to a procedure that is now defined - fixed
        String code = """
                define myProc
                move (10) steps
                
                when green flag clicked
                myProc
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CallWithoutDefinitionFix finder = new CallWithoutDefinitionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ProcedureStillUndefined() throws IOException, ParsingException {
        // Procedure definition still missing
        String code = """
                when green flag clicked
                myProc
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        CallWithoutDefinitionFix finder = new CallWithoutDefinitionFix(createTestIssueDTO(blockId));
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

        CallWithoutDefinitionFix finder = new CallWithoutDefinitionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testInjector_CallWithoutDefinition() throws IOException, ParsingException {
        // Start with a valid program (defined call)
        String fixedCode = """
                define myProc
                move (10) steps
                
                when green flag clicked
                myProc
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Remove definition of 'myProc'
        de.uni_passau.fim.se2.litterbox.injection.bugs.CallWithoutDefinitionInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.CallWithoutDefinitionInjector();

        Program buggyProgram = injector.inject(fixedProgram).get(0).mutatedProgram();

        // Verify the bug is found
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> issues =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program has definition)
        de.uni_passau.fim.se2.litterbox.analytics.Issue issue = issues.iterator().next();
        String bugID = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        CallWithoutDefinitionFix heuristic = new CallWithoutDefinitionFix(createTestIssueDTO(bugID));
        assertFixFound(heuristic, fixedProgram);
    }
}
