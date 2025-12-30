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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for EndlessRecursionFix.
 * Bug: Procedure calls itself without termination condition.
 * Fix: Recursive call wrapped in conditional or removed.
 */
public class EndlessRecursionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_RecursionInConditional() throws IOException, ParsingException {
        // Recursive call now in a conditional - fixed
        String code = """
                define recurse
                if <key (space v) pressed?> then
                    recurse
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        EndlessRecursionFix finder = new EndlessRecursionFix(blockId);
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_BugStillPresent() throws IOException, ParsingException {
        // Recursive call is still unconditional
        String code = """
                define recurse
                recurse
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, CallStmt.class);

        EndlessRecursionFix finder = new EndlessRecursionFix(blockId);
        assertNoFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                define other
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        EndlessRecursionFix finder = new EndlessRecursionFix(blockId);
        assertNoFixFound(finder, program);
    }
}
