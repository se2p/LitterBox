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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELDS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorLookStmtOpcode.looks_nextbackdrop;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode.looks_nextcostume;

public class ElementChoiceParser {

    private static final String BACKDROP = "BACKDROP";

    public static ElementChoice parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        if (opcodeString.equals(looks_nextcostume.toString()) || opcodeString.equals(looks_nextbackdrop.toString())) {
            return new Next();
        }

        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        final JsonNode inputsNode = inputsList.get(0);

        if (getShadowIndicator((ArrayNode) inputsNode) == 1) {
            return getElementChoiceFromMenu(allBlocks, inputsNode);
        } else {
            final Expression expression = ExpressionParser.parseExpression(current, BACKDROP, allBlocks);
            return new WithExpr(expression);
        }
    }

    private static ElementChoice getElementChoiceFromMenu(JsonNode allBlocks, JsonNode inputsNode) {
        String blockMenuID = inputsNode.get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuID);

        List<JsonNode> fieldsList = new ArrayList<>();
        menu.get(FIELDS_KEY).elements().forEachRemaining(fieldsList::add);
        String elementName = fieldsList.get(0).get(0).asText();

        String[] split = elementName.split(" ");
        String elemKey;
        if (split.length > 0) {
            elemKey = split[0];
        } else {
            elemKey = "";
        }

        if (!StandardElemChoice.contains(elemKey)) {
            return new WithExpr(new AsString(new StrId(elementName)));
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

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
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
