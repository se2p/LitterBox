/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ComparingLiteralsFix heuristic.
 *
 * <p>The bug is reported when a comparison compares two literals (always the same result).
 * The fix is detected when at least one operand is no longer a literal.
 */
public class ComparingLiteralsFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_BiggerThanWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable (x position)
        String source = """
                when green flag clicked
                if <(x position) > (50)> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_LessThanWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable
        String source = """
                when green flag clicked
                if <(myVar) < (100)> then
                    say [Below 100]
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, LessThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_EqualsWithVariable() throws IOException, ParsingException {
        // Fix: One operand is now a variable
        String source = """
                when green flag clicked
                if <(answer) = [yes]> then
                    say [Correct!]
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Equals.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_BothLiterals() throws IOException, ParsingException {
        // No Fix: Both operands are still literals
        String source = """
                when green flag clicked
                if <(50) > (40)> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, BiggerThan.class);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy comparison was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        ComparingLiteralsFix heuristic = new ComparingLiteralsFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
