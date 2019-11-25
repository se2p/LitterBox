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
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.elementchoice.*;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.num.UnspecifiedNumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.expression.string.UnspecifiedStringExpr;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.BoolExprOpcode;
import scratch.ast.opcodes.NumExprOpcode;
import scratch.ast.opcodes.StringExprOpcode;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.opcodes.SpriteLookStmtOpcode.looks_nextcostume;

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

        if (!menu.has(OPCODE_KEY)) {
            // Variable or List?
            throw new RuntimeException("Not implemented yet");
        } else if (NumExprOpcode.contains(menu.get(OPCODE_KEY).asText()) ||
            BoolExprOpcode.contains(menu.get(OPCODE_KEY).asText())) {

            NumExpr numExpr;
            try {
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
            } catch (ParsingException e) {
                numExpr = new UnspecifiedNumExpr();

            }
            return new WithNumber(numExpr);
        } else if (StringExprOpcode.contains(menu.get(OPCODE_KEY).asText())) {
            //Todo check if this case also covers variables
            StringExpr strExpr;
            try {
                strExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
            } catch (ParsingException e) {
                strExpr = new UnspecifiedStringExpr();

            }
            return new WithId(strExpr);
        }


        List<JsonNode> fieldsList = new ArrayList<>();
        menu.get(FIELDS_KEY).elements().forEachRemaining(fieldsList::add);
        String elementName = fieldsList.get(0).get(0).asText();

        String elemKey = elementName.split(" ")[0];
        if (!StandardElemChoice.contains(elemKey)) {
            return new WithId(new StrId(elementName));
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
