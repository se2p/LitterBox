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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.BACKDROP_INPUT;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELDS_KEY;

public class ElementChoiceParser {

    public static ElementChoice parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        final JsonNode inputsNode = inputsList.get(0);

        if (getShadowIndicator((ArrayNode) inputsNode) == 1) {
            return getElementChoiceFromMenu(allBlocks, inputsNode);
        } else {
            final Expression expression = ExpressionParser.parseExpr(current, BACKDROP_INPUT, allBlocks);
            return new WithExpr(expression, new NoBlockMetadata());
        }
    }

    private static ElementChoice getElementChoiceFromMenu(JsonNode allBlocks, JsonNode inputsNode)
            throws ParsingException {
        String blockMenuId = inputsNode.get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuId);
        BlockMetadata metadata = BlockMetadataParser.parse(blockMenuId, menu);

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
            return new WithExpr(new StrId(elementName), metadata);
        }

        StandardElemChoice standardElemChoice = StandardElemChoice.valueOf(elemKey);
        switch (standardElemChoice) {
            case random:
                return new Random(metadata);
            case next:
                return new Next(metadata);
            case previous:
                return new Prev(metadata);
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
