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
package de.uni_passau.fim.se2.litterbox.ast.opcodes;

public enum ControlStmtOpcode {

    control_if, // "if" BoolExpr "then" StmtList
    control_if_else, // "if" BoolExpr "then" StmtList "else" StmtList
    control_repeat, // "repeat"  NumExpr "times" StmtList
    control_repeat_until, // "until" BoolExpr "repeat" StmtList
    control_forever; //"repeat" "forever" StmtList

    public static boolean contains(String opcode) {
        for (ControlStmtOpcode value : ControlStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
