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
package de.uni_passau.fim.se2.litterbox.ast.opcodes;

public enum EventOpcode implements Opcode {
    // Scratch opcodes
    event_whenflagclicked,
    event_whenkeypressed,
    event_whenstageclicked,
    event_whenthisspriteclicked,
    event_whenbroadcastreceived,
    event_whenbackdropswitchesto,
    event_whengreaterthan,
    control_start_as_clone,

    // opcodes of educational robots
    when_board_launch,      // codey, mcore, auriga
    main,                   // megapi_robot, megapi
    when_button_press,      // codey
    when_board_button,      // mcore
    when_board_shake,       // codey
    when_board_tilt,        // codey
    when_volume_over,       // codey
    when_brightness_less;   // codey

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (EventOpcode value : EventOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static EventOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
