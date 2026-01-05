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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.ParameterAsLiteralInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for ParameterAsLiteralFix.
 * Bug: String literal matches a parameter name.
 * Fix: The literal no longer matches any parameter name.
 */
public class ParameterAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LiteralChanged() throws IOException, ParsingException {
        // Literal "hello" in procedure body doesn't match parameter "x" - fix reported
        String code = """
                define myBlock (x)
                say [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "hello".equals(lit.getText()));

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_LiteralStillMatches() throws IOException, ParsingException {
        // Literal "x" still matches parameter "x"
        String code = """
                define myBlock (x)
                say [x]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "x".equals(lit.getText()));

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(createTestIssueDTO(blockId));
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

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ParamAsLiteral() throws IOException, ParsingException {
        // Start with a valid program (using parameter variable, not literal)
        String fixedCode = """
                define myBlock (x)
                say (x)
                """;
        Program fixedProgram = parseProgram(fixedCode);

        // Verify clean
        ParameterAsLiteral bugFinder = new ParameterAsLiteral();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original program should be clean");

        // Inject bug (swaps parameter usage with string literal of same name)
        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(), "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(), "Mutated program should have bugs");

        // Fix check: The bug is the string literal 'x'. In the original program, that literal is NOT present
        // (it's a parameter reporter). So checking for the *literal's* ID in the original program
        // should return empty (assuming IDs are preserved/mapped or purely based on object identity/location).
        // Since we are simulating "deletion" or "replacement back to variable", if we look for the *literal*
        // in the original (where it was a variable), it won't be found as that specific literal node.
        String bugBlockId = AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());

        ParameterAsLiteralFix fix = new ParameterAsLiteralFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
