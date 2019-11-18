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
package scratch.newast.parser.attributes;

public enum DragMode {
    not_draggable, draggable;

    @Override
    public String toString() {
        if(this==not_draggable){
            return("not draggable");
        }else{
            return ("draggable");
        }
    }

    public static boolean contains(String opcode) {
        for (DragMode value : DragMode.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
