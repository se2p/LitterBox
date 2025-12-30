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

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for NoWorkingScriptsFix.
 *
 * <p>Bug condition: An actor has BOTH:
 * <ul>
 *   <li>Dead code: script without event handler but with statements</li>
 *   <li>Empty script: event handler with no statements</li>
 * </ul>
 *
 * <p>Fix: Either add statements to the event handler, or remove the dead code.
 */
public class NoWorkingScriptsFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_EventScriptNowHasStatements() throws IOException, ParsingException {
        // Fixed: The event handler now has statements (move step added)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        // Use any block ID from the script as the bug location
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps.class);

        NoWorkingScriptsFix finder = new NoWorkingScriptsFix(blockId);
        assertFixFound(finder, program);
    }



    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Scripts are gone (or actor is clean)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        NoWorkingScriptsFix finder = new NoWorkingScriptsFix(blockId);
        assertNoFixFound(finder, program);
    }
}
