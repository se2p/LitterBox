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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ForeverInsideIfFix.
 * Bug: RepeatForever inside If with following statements.
 * Fix: No longer inside If or no statements follow.
 */
public class ForeverInsideIfFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NotInsideIf() throws IOException, ParsingException {
        // RepeatForever not inside an If statement - fixed
        String code = """
                when green flag clicked
                forever
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideIfFix finder = new ForeverInsideIfFix(blockId);
        assertFixFound(finder, program);
    }
}
