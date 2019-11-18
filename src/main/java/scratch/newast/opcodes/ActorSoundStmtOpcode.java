/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.newast.opcodes;

public enum ActorSoundStmtOpcode {

    sound_playuntildone,    //    "play" "sound"  ElementChoice  "until" "done"
    sound_play,             // |  "start" "sound"  ElementChoice
    sound_cleareffects,     // |  "clear" "sound" "effects"
    sound_stopallsounds;    // |  "stop" "all" "sounds"

    public static boolean contains(String opcode) {
        for (ActorSoundStmtOpcode value : ActorSoundStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
