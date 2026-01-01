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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FloatingPointComparisonFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundOutsideCondition() throws IOException, ParsingException {
        // Fix: Equals block NOT used as a condition (e.g. dropped where string/number expected)
        // Note: In ScratchBlocks text, we can put boolean reporters in non-boolean slots.
        // "say <(mouse x) = (mouse y)>"
        String source = """
                when green flag clicked
                say <(mouse x) = (mouse y)>
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundSafeOperands() throws IOException, ParsingException {
        // Fix: Equals block inside condition, but operands are safe (e.g., strings)
        String source = """
                when green flag clicked
                if <(username) = [some user]> then
                end
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixUnsafeInsideCondition() throws IOException, ParsingException {
        // No Fix: Equals block inside condition check (if/repeat until) AND operands are unsafe (e.g., mouse x)
        String source = """
                when green flag clicked
                if <(mouse x) = (mouse y)> then
                end
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Equals.class);
        
        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(createTestIssueDTO(bugLocationBlockId));
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

        FloatingPointComparisonFix heuristic = new FloatingPointComparisonFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_FloatingPointComparison() throws IOException, ParsingException {
        // Start with valid program (comparing literal values, not unsafe operands)
        String fixedCode = """
                when green flag clicked
                if <(10) = (20)> then
                    say [Match!]
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds unsafe operand like PositionX)
        de.uni_passau.fim.se2.litterbox.injection.bugs.FloatingPointComparisonInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.FloatingPointComparisonInjector();
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

        // Use existing Equals block ID from original program
        String existingBlockId = findBlockId(fixedProgram, Equals.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find Equals in original program");

        FloatingPointComparisonFix fix = new FloatingPointComparisonFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
