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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MessageAsLiteralFix heuristic.
 *
 * <p>The bug is reported when a string literal matches a message name but isn't broadcast.
 * The fix is detected when:
 * <ul>
 *   <li>The literal is no longer a defined message name</li>
 *   <li>The message is now properly sent via broadcast</li>
 * </ul>
 */
public class MessageAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MessageNotDefined() throws IOException, ParsingException {
        // Fix: String literal "myMessage" is NOT a defined message (no receive handler)
        String source = """
                when green flag clicked
                say [myMessage]
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_MessageNowSent() throws IOException, ParsingException {
        // Fix: String literal matches message AND the message is now broadcast
        String source = """
                when green flag clicked
                say [myMessage]
                broadcast (myMessage v)
                
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        // Find the StringLiteral inside the say block, not the broadcast/receive
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage") && s.getParentNode().getClass().getSimpleName().contains("Say"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_DefinedButNotSent() throws IOException, ParsingException {
        // No Fix: Message IS defined (has receive handler) but NOT sent
        String source = """
                when green flag clicked
                say [myMessage]
                
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage") && s.getParentNode().getClass().getSimpleName().contains("Say"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block no longer exists
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
