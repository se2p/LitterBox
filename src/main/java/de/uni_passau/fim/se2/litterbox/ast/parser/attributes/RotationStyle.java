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
package de.uni_passau.fim.se2.litterbox.ast.parser.attributes;

public enum RotationStyle {

    dont_rotate("don't rotate"),
    left_right("left-right"),
    all_around("all around");

    private final String token;

    RotationStyle(String token) {
        this.token = token;
    }

    public static boolean contains(String opcode) {
        for (RotationStyle value : RotationStyle.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return token;
    }
}
