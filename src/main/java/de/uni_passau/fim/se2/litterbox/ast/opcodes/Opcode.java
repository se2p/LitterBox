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

public interface Opcode {
    String getName();

    /**
     * Method to remove the prefixes used by mBlock.
     *
     * @param longString original opcode
     * @return opcode without the prefix
     */
    static String removePrefix(String longString) {
        if (longString.contains(".")) {
            // splits first part away
            longString = longString.split("\\.")[1];
            // removes second part of prefix depending on robot
            longString = longString.replace("meos_", "");   // codey.meos
            longString = longString.replace("codey_", "");  // codey.codey
            longString = longString.replace("mcore_", "");  // mcore.mcore
            longString = longString.replace("auriga_", ""); // auriga.auriga
            longString = longString.replace("megapi_", ""); // megapi_robot.megapi; megapi.megapi
        }
        return longString;
    }
}
