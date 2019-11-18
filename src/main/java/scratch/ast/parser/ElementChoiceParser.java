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
import static scratch.ast.opcodes.SpriteLookStmtOpcode.looks_nextcostume;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import scratch.ast.Constants;
import scratch.ast.model.elementchoice.ElementChoice;
import scratch.ast.model.elementchoice.Next;
import scratch.ast.model.elementchoice.Prev;
import scratch.ast.model.elementchoice.Random;
import scratch.ast.model.elementchoice.WithId;
import scratch.ast.model.variable.Identifier;

public class ElementChoiceParser {

    public static ElementChoice parse(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        if (opcodeString.equals(looks_nextcostume.toString())) {
            return new Next();
        }

        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        String blockMenuID = inputsList.get(0).get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuID);

        List<JsonNode> fieldsList = new ArrayList<>();
        menu.get(FIELDS_KEY).elements().forEachRemaining(fieldsList::add);
        String elementName = fieldsList.get(0).get(0).asText();

        String elemKey = elementName.split(" ")[0];
        if (!StandardElemChoice.contains(elemKey)) {
            return new WithId(new Identifier(elementName));
        }

        StandardElemChoice standardElemChoice = StandardElemChoice.valueOf(elemKey);
        switch (standardElemChoice) {
            case random:
                return new Random();
            case next:
                return new Next();
            case previous:
                return new Prev();
            default:
                throw new RuntimeException("No implementation for " + standardElemChoice);
        }
    }

    private enum StandardElemChoice {
        random, next, previous;

        public static boolean contains(String opcode) {
            for (StandardElemChoice value : StandardElemChoice.values()) {
                if (value.name().equals(opcode)) {
                    return true;
                }
            }
            return false;
        }
    }
}
