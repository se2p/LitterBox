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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.FloatingPointComparisonInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
        FloatingPointComparison bugFinder = new FloatingPointComparison();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds unsafe operand like PositionX)
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Use existing Equals block ID from original program
        String existingBlockId = findBlockId(fixedProgram, Equals.class);
        Assertions.assertNotNull(existingBlockId,
                "Should find Equals in original program");

        FloatingPointComparisonFix fix = new FloatingPointComparisonFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
