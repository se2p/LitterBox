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

public enum BoolExprOpcode implements Opcode {

    sensing_touchingobject,
    sensing_touchingcolor,
    sensing_coloristouchingcolor,
    sensing_keypressed,
    sensing_mousedown,
    operator_gt,
    operator_lt,
    operator_equals,
    operator_and,
    operator_or,
    operator_not,
    operator_contains,
    data_listcontainsitem,

    // mBlock opcodes
    // codey
    event_led_matrix_position_is_light,
    event_button_pressed,
    event_connect_rocky,
    event_is_shaked,
    event_is_tilt,
    event_is_orientate_to,
    rocky_event_obstacles_ahead,
    event_is_color,

    // mcore
    event_external_linefollower,
    event_board_button_pressed,
    event_ir_remote;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (BoolExprOpcode value : BoolExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static BoolExprOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
