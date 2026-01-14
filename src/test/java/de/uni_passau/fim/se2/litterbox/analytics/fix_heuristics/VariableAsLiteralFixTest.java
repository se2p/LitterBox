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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for VariableAsLiteralFix.
 * Bug: String literal matches a variable name.
 * Fix: The literal no longer matches any variable name in scope.
 */
public class VariableAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LiteralChanged() throws IOException, ParsingException {
        // Literal "hello" doesn't match any variable - fix reported
        String code = """
                when green flag clicked
                say [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "hello".equals(lit.getText()));

        VariableAsLiteralFix finder = new VariableAsLiteralFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_LiteralMatchesVariable() throws IOException, ParsingException {
        // Literal "hello" matches variable name
        String code = """
                when green flag clicked
                set [hello v] to (0)
                say [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "hello".equals(lit.getText()));

        VariableAsLiteralFix finder = new VariableAsLiteralFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
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

        VariableAsLiteralFix finder = new VariableAsLiteralFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testInjector_VariableAsLiteral() throws IOException, ParsingException {
        // Start with a fixed program - usage of a variable
        String fixedCode = """
                when green flag clicked
                set [myVar v] to (0)
                say (myVar)""";

        Program fixedProgram = parseProgram(fixedCode);

        // Inject bug
        de.uni_passau.fim.se2.litterbox.injection.bugs.VariableAsLiteralInjector injector = new de.uni_passau.fim.se2.litterbox.injection.bugs.VariableAsLiteralInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> injected = injector.inject(fixedProgram);

        org.junit.jupiter.api.Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has the bug
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral bugFinder = new de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues = bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(), "Bug finder should detect VariableAsLiteral in mutated program");

        // Get the block ID
        de.uni_passau.fim.se2.litterbox.analytics.Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(bugIssue.getCodeLocation());

        // The fix heuristic should detect a fix in the original program
        VariableAsLiteralFix fixHeuristic = new VariableAsLiteralFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
