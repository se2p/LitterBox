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

public enum NumExprOpcode implements Opcode {
    operator_add,
    operator_subtract,
    operator_multiply,
    operator_divide,
    operator_mod,
    operator_random,
    operator_round,
    operator_length,
    data_lengthoflist,
    data_itemnumoflist,
    sensing_timer,
    sensing_dayssince2000,
    sensing_current,
    sensing_distanceto,
    sensing_mousex,
    sensing_mousey,
    sensing_loudness,
    operator_mathop,
    sound_volume,
    motion_xposition,
    motion_yposition,
    motion_direction,
    looks_size,

    // mBlock Number Opcodes
    // codey
    detect_sound_volume,
    detect_potentiometer,
    detect_volume,
    detect_lightness,
    dump_energy,
    detect_shaked_strength,
    detect_gyro_roll_angle,
    detect_gyro_pitch_angle,
    detect_rotatex_angle,
    detect_rotatey_angle,
    detect_rotatez_angle,
    detect_time,
    rocky_detect_rgb,
    rocky_detect_lightness,
    rocky_detect_reflection,
    rocky_detect_ir_reflection,
    rocky_detect_grey,

    // mcore
    detect_external_light,
    detect_external_ultrasonic,
    detect_external_linefollower,
    detect_timer,

    //music
    music_getTempo;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (NumExprOpcode value : NumExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static NumExprOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
