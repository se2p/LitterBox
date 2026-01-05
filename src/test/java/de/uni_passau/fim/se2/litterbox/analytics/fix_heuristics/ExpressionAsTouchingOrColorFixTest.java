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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExpressionAsTouchingOrColorFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundColorTouchingColor() throws IOException, ParsingException {
        // Fix: Operands are ColorLiteral
        // "say <color [#FF0000] is touching [#00FF00] ?>"
        String source = """
                when green flag clicked
                say <color [#FF0000] is touching [#00FF00] ?>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, ColorTouchingColor.class);

        ExpressionAsTouchingOrColorFix heuristic = new ExpressionAsTouchingOrColorFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixColorTouchingColor() throws IOException, ParsingException {
        // No Fix: Operands are expressions (e.g., variables), not ColorLiterals
        // "say <color (mouse x) is touching (mouse y) ?>"
        String source = """
                when green flag clicked
                say <color (mouse x) is touching (mouse y) ?>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, ColorTouchingColor.class);

        ExpressionAsTouchingOrColorFix heuristic = new ExpressionAsTouchingOrColorFix(createTestIssueDTO(bugLocationBlockId));
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

        ExpressionAsTouchingOrColorFix heuristic = new ExpressionAsTouchingOrColorFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ExpressionAsTouchingOrColor() throws IOException, ParsingException {
        // Start with valid program (color literals in color blocks)
        String fixedCode = """
                when green flag clicked
                if <color [#FF0000] is touching [#00FF00] ?> then
                    say [Colors matched!]
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (replaces ColorLiteral with variable expression)
        de.uni_passau.fim.se2.litterbox.injection.bugs.ExpressionAsTouchingOrColorInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.ExpressionAsTouchingOrColorInjector();
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

        // Use existing ColorTouchingColor block ID from original program
        String existingBlockId = findBlockId(fixedProgram, ColorTouchingColor.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find ColorTouchingColor in original program");

        ExpressionAsTouchingOrColorFix fix = new ExpressionAsTouchingOrColorFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
