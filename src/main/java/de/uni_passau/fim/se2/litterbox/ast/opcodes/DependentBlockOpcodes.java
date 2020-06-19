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

/**
 * These enums are for blocks which are only used inside other blocks and thus do not have to be parsed as statements
 * themselves.
 */
public enum DependentBlockOpcodes {

    motion_goto_menu, motion_glideto_menu, motion_pointtowards_menu,
    looks_costume, looks_backdrops, sound_sounds_menu, control_create_clone_of_menu,
    sensing_distancetomenu, sensing_touchingobjectmenu, sensing_keyoptions,
    sensing_of_object_menu, pen_menu_colorParam;

    public static boolean contains(String opcode) {
        for (DependentBlockOpcodes value : DependentBlockOpcodes.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
