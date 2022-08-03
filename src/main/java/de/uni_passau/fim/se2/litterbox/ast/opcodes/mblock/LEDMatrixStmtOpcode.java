/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum LEDMatrixStmtOpcode implements Opcode {
    // codey
    show_led_matrix_face_with_time,
    show_led_matrix_face,
    show_led_matrix_face_position,
    show_led_matrix_turn_off,
    show_led_matrix,
    show_led_matrix_string,
    show_led_matrix_string_position,
    show_led_matrix_position,
    light_off_led_matrix_position,
    toggle_led_matrix_position,

    // mcore
    show_face_time,
    show_face,
    show_face_position,
    show_text,
    show_text_position,
    show_number,
    show_time,
    show_face_off;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (LEDMatrixStmtOpcode value : LEDMatrixStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static LEDMatrixStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
