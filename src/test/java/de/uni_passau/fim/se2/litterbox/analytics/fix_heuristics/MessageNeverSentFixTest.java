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
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MessageNeverSentFix heuristic.
 *
 * <p>The bug is reported when a message handler exists but the message is never sent.
 * The fix is detected when the message is now sent.
 */
public class MessageNeverSentFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MessageNowSent() throws IOException, ParsingException {
        // Fix: Message handler's message is now sent via broadcast
        String source = """
                when green flag clicked
                broadcast (myMessage v)
                
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ReceptionOfMessage.class);

        MessageNeverSentFix heuristic = new MessageNeverSentFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_MessageNotSent() throws IOException, ParsingException {
        // No Fix: Handler exists but message is never broadcast
        String source = """
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ReceptionOfMessage.class);

        MessageNeverSentFix heuristic = new MessageNeverSentFix(bugId);
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

        MessageNeverSentFix heuristic = new MessageNeverSentFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
