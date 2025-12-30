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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for KeySetPositionFix heuristic.
 *
 * <p>The bug is reported when an arrow key event uses SetX/SetY (absolute positioning)
 * instead of ChangeX/ChangeY (relative movement). Specifically:
 * <ul>
 *   <li>SetX with LEFT/RIGHT arrows is buggy</li>
 *   <li>SetY with UP/DOWN arrows is buggy</li>
 * </ul>
 *
 * <p>The fix is detected when:
 * <ul>
 *   <li>SetX/SetY is outside an arrow key context</li>
 *   <li>SetX is used with UP/DOWN arrows (mismatched pattern)</li>
 *   <li>SetY is used with LEFT/RIGHT arrows (mismatched pattern)</li>
 *   <li>Any non-arrow key is used</li>
 * </ul>
 */
public class KeySetPositionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoKeyContext() throws IOException, ParsingException {
        // Fix: SetX used outside of any key context
        String source = """
                when green flag clicked
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_NonArrowKey() throws IOException, ParsingException {
        // Fix: SetX used with space key (not an arrow key)
        String source = """
                when [space v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_SetXWithUpArrow() throws IOException, ParsingException {
        // Fix: SetX used with UP arrow (bug is SetX with LEFT/RIGHT)
        String source = """
                when [up arrow v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_SetYWithLeftArrow() throws IOException, ParsingException {
        // Fix: SetY used with LEFT arrow (bug is SetY with UP/DOWN)
        String source = """
                when [left arrow v] key pressed
                set y to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetYTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SetXWithLeftArrow() throws IOException, ParsingException {
        // No Fix: SetX used with LEFT arrow (this IS the bug pattern)
        String source = """
                when [left arrow v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SetYWithUpArrow() throws IOException, ParsingException {
        // No Fix: SetY used with UP arrow (this IS the bug pattern)
        String source = """
                when [up arrow v] key pressed
                set y to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetYTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_IfKeyPressed_DifferentKey() throws IOException, ParsingException {
        // Fix: SetX in if(space pressed) - not an arrow key
        String source = """
                when green flag clicked
                if <key (space v) pressed?> then
                    set x to (0)
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block was deleted entirely
        String source = """
                when [left arrow v] key pressed
                move (10) steps
                """;

        Program program = parseProgram(source);

        KeySetPositionFix heuristic = new KeySetPositionFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
