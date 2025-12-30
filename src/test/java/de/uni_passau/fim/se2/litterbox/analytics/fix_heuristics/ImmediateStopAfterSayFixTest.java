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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ImmediateStopAfterSayFix.
 * Bug: Say/Think block followed immediately by StopAll.
 * Fix: StopAll removed or intermediate block added.
 */
public class ImmediateStopAfterSayFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoNextStmt() throws IOException, ParsingException {
        // Fix: Say is last statement (No subsequent StopAll)
        String code = """
                when green flag clicked
                say [Hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(blockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_NextStmtNotStopAll() throws IOException, ParsingException {
        // Fix: Next statement is MoveSteps (NOT StopAll)
        String code = """
                when green flag clicked
                say [Hello]
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(blockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SayFollowedByStopAll() throws IOException, ParsingException {
        // No Fix: Next statement *IS* StopAll
        String code = """
                when green flag clicked
                say [Hello]
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(blockId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ThinkNoNextStmt() throws IOException, ParsingException {
        // Fix: Think is last statement
        String code = """
                when green flag clicked
                think [Hmm]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Think.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(blockId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(blockId);
        assertNoFixFound(heuristic, program);
    }
}
