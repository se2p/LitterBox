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
package de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum EmotionStmtOpcode implements Opcode {
    emotion_look_up,
    emotion_look_down,
    emotion_look_left,
    emotion_look_right,
    emotion_look_around,
    emotion_wink,
    emotion_smile,
    emotion_yeah,
    emotion_naughty,
    emotion_proud,
    emotion_coquetry,
    emotion_awkward,
    emotion_exclaim,
    emotion_aggrieved,
    emotion_sad,
    emotion_angry,
    emotion_greeting,
    emotion_sprint,
    emotion_startle,
    emotion_shiver,
    emotion_dizzy,
    emotion_sleepy,
    emotion_sleeping,
    emotion_revive,
    emotion_agree,
    emotion_deny;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (EmotionStmtOpcode value : EmotionStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static EmotionStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
