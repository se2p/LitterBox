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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MessageNeverReceivedFix heuristic.
 *
 * <p>The bug is reported when a broadcast is sent but never received.
 * The fix is detected when the message is now received.
 */
public class MessageNeverReceivedFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MessageNowReceived() throws IOException, ParsingException {
        // Fix: Broadcast message is now received by a handler
        String source = """
                when green flag clicked
                broadcast (myMessage v)
                
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Broadcast.class);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_MessageNotReceived() throws IOException, ParsingException {
        // No Fix: Broadcast is sent but no handler receives it
        String source = """
                when green flag clicked
                broadcast (myMessage v)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Broadcast.class);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
