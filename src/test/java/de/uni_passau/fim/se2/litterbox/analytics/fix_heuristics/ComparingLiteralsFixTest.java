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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.bugs.ComparingLiteralsInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

/**
 * Tests for ComparingLiteralsFix heuristic.
 *
 * <p>The bug is reported when a comparison compares two literals (always the same result).
 * The fix is detected when at least one operand is no longer a literal.
 */
class ComparingLiteralsFixTest extends AbstractFixHeuristicTest {

    @Test
    void testFixFound_BiggerThanWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable (x position)
        String source = """
                when green flag clicked
                if <(x position) > (50)> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    void testFixFound_LessThanWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable
        String source = """
                when green flag clicked
                if <(myVar) < (100)> then
                    say [Below 100]
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, LessThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    void testFixFound_EqualsWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable
        String source = """
                when green flag clicked
                if <(answer) = [yes]> then
                    say [Correct!]
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Equals.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    void testNoFix_BothLiterals() throws IOException, ParsingException {
        // No Fix: Both operands are still literals
        String source = """
                when green flag clicked
                if <(50) > (40)> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy comparison was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // NESTED STRUCTURE TESTS
    // ========================================================================

    @Test
    void testNoFix_BothLiteralsInNestedIf() throws IOException, ParsingException {
        // No Fix: Comparison with both literals inside nested structure
        String source = """
                when green flag clicked
                if <touching (edge v)?> then
                    if <(100) > (50)> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    void testFixFound_NestedComparisonWithVariable() throws IOException, ParsingException {
        // Fix: Comparison now uses variable, even when deeply nested
        String source = """
                when green flag clicked
                repeat (10)
                    if <touching (edge v)?> then
                        if <(timer) > (5)> then
                            say [Time's up!]
                        end
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    void testInjector_ComparingLiterals() throws IOException, ParsingException {
        // Start with a valid program (comparisons involving variables)
        String fixedCode = """
                when green flag clicked
                if <(x position) > (10)> then
                    move (10) steps
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Inject the bug: Change comparison to use literals e.g. 5 > 10
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();

        Program buggyProgram = injector.inject(fixedProgram).getFirst().mutatedProgram();

        // Verify the bug is found
        Set<Issue> issues = new ComparingLiterals().check(buggyProgram);
        Assertions.assertFalse(issues.isEmpty(), "Injector should create at least one bug");

        // Verify the fix heuristic finds the fix (original program uses variable)
        Issue issue = issues.iterator().next();
        String bugID = AstNodeUtil.getBlockId(issue.getNormalizedCodeLocation());

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(createTestIssueDTO(bugID));
        assertFixFound(heuristic, fixedProgram);
    }
}
