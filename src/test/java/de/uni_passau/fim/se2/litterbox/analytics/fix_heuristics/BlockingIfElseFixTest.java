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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for BlockingIfElseFix.
 * Bug: If-Else with forever in one branch blocks following code.
 * Fix: Forever removed or following code removed.
 */
public class BlockingIfElseFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoForeverInBranch() throws IOException, ParsingException {
        // If-Else without forever loop in branches - fixed
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                    move (10) steps
                else
                    turn cw (15) degrees
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IfElseStmt.class);

        BlockingIfElseFix finder = new BlockingIfElseFix(blockId);
        assertFixFound(finder, program);
    }
}
