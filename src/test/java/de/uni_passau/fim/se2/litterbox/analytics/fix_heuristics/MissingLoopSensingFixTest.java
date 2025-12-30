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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingLoopSensingFix.
 * Bug: Sensing blocks used outside a loop.
 * Fix: Sensing block is now inside a loop or after wait/ask.
 */
public class MissingLoopSensingFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_InsideLoop() throws IOException, ParsingException {
        // Key pressed inside forever loop - fix reported
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingFix finder = new MissingLoopSensingFix(blockId);
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_AfterWaitUntil() throws IOException, ParsingException {
        // Sensing after wait until - fix reported
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                if <key (space v) pressed?> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        // Find the second IsKeyPressed (the one in the if)
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingFix finder = new MissingLoopSensingFix(blockId);
        assertFixFound(finder, program);
    }
}
