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

public enum CommonStmtOpcode {

    control_wait,            //"wait" NumExpr "seconds"
    control_wait_until,      //"wait" "until" BoolExpr
    control_stop,            //"stop" "other" "scripts" "in" "sprite"
    control_create_clone_of, // "create" "clone" "of" Ident
    event_broadcast,         // "broadcast" Message
    event_broadcastandwait,  // "broadcast" Message "and" "wait"
    sensing_resettimer,      // "reset" "timer"
    data_changevariableby;    // "change" Variable "by" Expression

    public static boolean contains(String opcode) {
        for (CommonStmtOpcode value : CommonStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
