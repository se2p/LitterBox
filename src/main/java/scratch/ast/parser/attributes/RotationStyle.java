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
package scratch.ast.parser.attributes;

public enum RotationStyle {
    dont_rotate, left_right, all_around;

    @Override
    public String toString() {
        if (this == dont_rotate) {
            return "don't rotate";
        } else if (this == left_right) {
            return "left-right";
        } else {
            return "all around";
        }
    }

    public static boolean contains(String opcode) {
        for (RotationStyle value : RotationStyle.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
