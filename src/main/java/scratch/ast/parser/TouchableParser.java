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

import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.INPUTS_KEY;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.ast.model.touchable.Edge;
import scratch.ast.model.touchable.MousePointer;
import scratch.ast.model.touchable.Touchable;
import scratch.ast.model.variable.Identifier;
import scratch.ast.opcodes.BoolExprOpcode;

public class TouchableParser {

    public static final String TOUCHINGOBJECTMENU = "TOUCHINGOBJECTMENU";

    public static Touchable parseTouchable(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        String opcodeString = current.get(OPCODE_KEY).asText();

        if (BoolExprOpcode.sensing_touchingobject.name().equals(opcodeString)) {
            String menuID = current.get(INPUTS_KEY).get(TOUCHINGOBJECTMENU).get(POS_INPUT_VALUE).asText();
            String touchingobject = allBlocks.get(menuID).get(FIELDS_KEY).get(TOUCHINGOBJECTMENU).get(0).asText();

            if (touchingobject.equals("_mouse_")) {
                return new MousePointer();
            } else if (touchingobject.equals("_edge_")) {
                return new Edge();
            } else {
                return new Identifier(touchingobject);
            }

        } else if (BoolExprOpcode.sensing_touchingcolor.name().equals(opcodeString)) {
            return ColorParser.parseColor(current, 0, allBlocks);
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }
}
