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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELDS_KEY;

public class CostumeChoiceParser {

    private static final String COSTUME_INPUT = "COSTUME";

    public static Expression parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        final JsonNode inputsNode = inputsList.get(0);
        if (getShadowIndicator((ArrayNode) inputsNode) == 2) {
            System.out.println("here");
        }
        if (getShadowIndicator((ArrayNode) inputsNode) == 1) {
            return getCostumeChoiceFromMenu(allBlocks, inputsNode);
        } else {
            return ExpressionParser.parseExpression(current, COSTUME_INPUT, allBlocks);
        }
    }

    private static Expression getCostumeChoiceFromMenu(JsonNode allBlocks, JsonNode inputsNode) {
        String blockMenuID = inputsNode.get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuID);

        List<JsonNode> fieldsList = new ArrayList<>();
        menu.get(FIELDS_KEY).elements().forEachRemaining(fieldsList::add);
        String elementName = fieldsList.get(0).get(0).asText();
        return new StrId(elementName); // TODO use qualified here?
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
