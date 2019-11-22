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
package scratch.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.ast.model.Key;
import scratch.ast.opcodes.BoolExprOpcode;

import static scratch.ast.Constants.*;

public class KeyParser {

    public static final String KEY_OPTION = "KEY_OPTION";

    public static Key parse(JsonNode current, JsonNode allBlocks) {

        final JsonNode block;
        final String opcodeString = current.get(OPCODE_KEY).asText();
        if (BoolExprOpcode.sensing_keypressed.name().equals(opcodeString)) {
            String menuBlockID = current.get(INPUTS_KEY).get(KEY_OPTION).get(POS_INPUT_VALUE).asText();
            block = allBlocks.get(menuBlockID);
        } else {
            block = current;
        }

        String keyValue = block.get(FIELDS_KEY).get(KEY_OPTION).get(FIELD_VALUE).asText();
        return new Key(keyValue);
    }

}
