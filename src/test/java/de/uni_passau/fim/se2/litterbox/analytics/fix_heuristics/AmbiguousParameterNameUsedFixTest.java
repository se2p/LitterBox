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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class AmbiguousParameterNameUsedFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Custom block parameters are UNIQUE (param1, param2)
        String source = """
                define myProc (param1) (param2)
                """;

        try {
            Program program = parseProgram(source);
            String bugLocationBlockId = findBlockId(program, ProcedureDefinition.class);

            AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(createTestIssueDTO(bugLocationBlockId));
            assertFixFound(heuristic, program);
        } catch (ParsingException e) {
            // Fallback if ScratchBlocks parser doesn't support 'define' or fails
            // But ideally it should work. If not, we might need manual construction.
            throw e;
        }
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Custom block parameters are DUPLICATE (param1, param1)
        // We use the injector to ensure the AST has duplicates/ambiguity,
        // as the parser might normalize invalid ScratchBlocks code.
        String fixedCode = """
                define myProc (param1) (param2)
                say (param1)
                say (param2)
                """;
        Program program = parseProgram(fixedCode);

        // Inject the bug
        de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.AmbiguousParameterNameUsedInjector();

        Program buggyProgram = injector.inject(program).getFirst().mutatedProgram();

        String bugLocationBlockId = findBlockId(buggyProgram, ProcedureDefinition.class);

        // Ensure the bug actually exists before testing the fix heuristic
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugs =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed().check(buggyProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugs.isEmpty(), "Injection failed - bug not found in buggyProgram");

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, buggyProgram);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testInjector_AmbiguousParameter() throws IOException, ParsingException {
        // Start with a valid program (unique parameters) AND USE THEM
        String fixedCode = """
                define myProc (param1) (param2)
                say (param1)
                say (param2)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Duplicate parameter names
        AmbiguousParameterNameUsedInjector injector = new AmbiguousParameterNameUsedInjector();

        Program buggyProgram = injector.inject(fixedProgram).getFirst().mutatedProgram();

        // Verify the bug is found
        Set<Issue> issues = new AmbiguousParameterNameUsed().check(buggyProgram);
        Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program)
        Issue issue = issues.iterator().next();
        String bugID = AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        AmbiguousParameterNameUsedFix heuristic = new AmbiguousParameterNameUsedFix(createTestIssueDTO(bugID));
        assertFixFound(heuristic, fixedProgram);
    }
}
