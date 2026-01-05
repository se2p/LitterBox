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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IllegalParameterRefactorFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Parameter is defined as BOOLEAN (<myParam>) and used in boolean context
        String source = """
                define myProc <myParam>
                if <myParam> then
                end
                """;

        Program program = parseProgram(source);
        // Find the Parameter node usage, not definition.
        // Usage is inside the If statement.
        String bugLocationBlockId = findBlockId(program, Parameter.class);

        IllegalParameterRefactorFix heuristic = new IllegalParameterRefactorFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Parameter is defined as STRING/NUMBER ((myParam)) but used in boolean context
        // This implies the refactoring (changing to boolean) hasn't happened yet.
        String source = """
                define myProc (myParam)
                if <myParam> then
                end
                """;
        // Note: "<myParam>" usage refers to the parameter. If defined as (myParam),
        // ScratchBlocks parser might parse usage as <myParam> if it fits the slot,
        // but the SymbolTable record for "myParam" comes from definition (String).

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Parameter.class);

        IllegalParameterRefactorFix heuristic = new IllegalParameterRefactorFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
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

        IllegalParameterRefactorFix heuristic = new IllegalParameterRefactorFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_IllegalParameterRefactor() throws IOException, ParsingException {
        // Start with valid program (boolean parameter used as boolean)
        String fixedCode = """
                define myProc <myParam>
                if <myParam> then
                    say [True!]
                end

                when green flag clicked
                myProc <key (space v) pressed?>
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.IllegalParameterRefactor bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.IllegalParameterRefactor();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (changes boolean param type to string)
        de.uni_passau.fim.se2.litterbox.injection.bugs.IllegalParameterRefactorInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.IllegalParameterRefactorInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results =
                injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues =
                bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Use existing Parameter block ID from original program
        String existingBlockId = findBlockId(fixedProgram, Parameter.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find Parameter in original program");

        IllegalParameterRefactorFix fix = new IllegalParameterRefactorFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
