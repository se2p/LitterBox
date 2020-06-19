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

public enum SpriteMotionStmtOpcode {

    motion_movesteps,       // "move"  NumExpr  "steps"
    motion_turnright,       // |  "turn" "right"  NumExpr "degrees"
    motion_turnleft,        // |  "turn" "left"  NumExpr "degrees"
    motion_gotoxy,
    motion_goto,            // |  "go" "to"  Position
    motion_glideto,
    motion_glidesecstoxy,   // |  "glide"  NumExpr  "secs" "to" Position
    motion_pointindirection,// |  "point" "in" "direction" NumExpr
    motion_pointtowards,    // |  "point" "towards"  Position
    motion_changexby,       // |  "change" "x" "by"  NumExpr
    motion_changeyby,       // |  "change" "y" "by"  NumExpr
    motion_setx,            // |  "set" "x" "to"  NumExpr
    motion_sety,            // |  "set" "y" "to"  NumExpr
    motion_ifonedgebounce,  // |  "if" "on" "edge" "bounce"
    motion_setrotationstyle,// |  "set rotation style" RotationStyle
    sensing_setdragmode;    // |  "set drag mode" DragMode

    public static boolean contains(String opcode) {
        for (SpriteMotionStmtOpcode value : SpriteMotionStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
