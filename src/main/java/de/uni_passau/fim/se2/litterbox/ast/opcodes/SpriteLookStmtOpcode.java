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

public enum SpriteLookStmtOpcode {

    looks_show,            //   "show"
    looks_hide,            //   |  "hide"
    looks_sayforsecs,      //   |  "say" StringExpr "for" NumExpr  "secs"
    looks_say,             //   |  "say" StringExpr
    looks_thinkforsecs,    //   |  "think" StringExpr "for" NumExpr  "secs"
    looks_think,           //   |  "think" StringExpr
    looks_switchcostumeto,    //   |  "switch" "costume" "to" ElementChoice
    looks_nextcostume,        //   |  "switch" "costume" "to" ElementChoice
    looks_changesizeby,       //   |  "change" "size" "by"  NumExpr
    looks_setsizeto,          //   |  "set" "size" "to"  NumExpr  "percent"
    looks_gotofrontback,      //   |  "go" "to" "layer"  NumExpr
    looks_goforwardbackwardlayers;    //   |  "change" "layer" "by"  NumExpr

    public static boolean contains(String opcode) {
        for (SpriteLookStmtOpcode value : SpriteLookStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
