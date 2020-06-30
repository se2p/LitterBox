/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;


import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;

/*
 * Documentation of syntax:
 * https://en.scratch-wiki.info/wiki/Block_Plugin/Syntax
 *
 * Every scratch block goes on a new line.
 * Example:
 *
 * [scratchblocks]
 * when green flag clicked
 * forever
 *     turn cw (15) degrees
 *     say [Hello!] for (2) secs
 *     if <mouse down?> then
 *         change [mouse clicks v] by (1)
 *     end
 * end
 * [/scratchblocks]
 */
public class ScratchBlocksVisitor implements ScratchVisitor {


    /*
     * The round numerical insert is used with parentheses: (10).
     * move (10) steps
     */
    @Override
    public void visit(StringType node) {
        // TODO
    }

    /*
     * String inputs are created with square brackets: [lorem ipsum]
     * say [Hi]
     */
    @Override
    public void visit(NumberType node) {
        // TODO
    }

    /*
     * Boolean blocks and reporter blocks are created with <boolean> and (reporter), respectively.
     */
    @Override
    public void visit(BooleanType node) {
        // TODO
    }

    /*
     * A color picker is represented with [#hexcode]. #hexcode is a hexadecimal color code.
     */
    @Override
    public void visit(Color node) {
        // TODO
    }

    /*
     * Dropdown lists are created with the code [selection v].
     */

    /*
     * The When Green Flag Clicked block can be typed with any of the following syntax options:
     * when green flag clicked
     * when gf clicked
     * when flag clicked
     *
     * Click on sprite:
     * when this sprite clicked
     *
     * Key press:
     * when [space v] key pressed
     *
     */

    // TODO: This is a dummy for now
    public String getScratchBlocks() {
        return "[scratchblocks]\n" +
                "when green flag clicked\n" +
                "todo\n" +
                "[/scratchblocks]\n";
    }
}
