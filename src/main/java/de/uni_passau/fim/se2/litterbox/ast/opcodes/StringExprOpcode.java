/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

public enum StringExprOpcode implements Opcode {

    operator_join,
    operator_letter_of,
    sensing_username,
    data_itemoflist,
    looks_costumenumbername,
    looks_backdropnumbername,
    sensing_answer,
    sensing_of,

    //mBlock String Opcode
    comm_receive_ir,    // codey
    detect_ir,         // mcore

    //translate

    translate_getViewerLanguage,
    translate_getTranslate;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (StringExprOpcode value : StringExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static StringExprOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
