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
package scratch.newast.parser;

import static scratch.newast.Constants.POS_DATA_ARRAY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.Expression;

public class ExpressionParser {

    public static Expression parseExpression(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        try {
            return NumExprParser.parseNumExpr(block, pos, blocks);
        } catch (Exception e) {
            try {
                return StringExprParser.parseStringExpr(block, pos, blocks);
            } catch (Exception ex) {
                try {
                    return BoolExprParser.parseBoolExpr(block, pos, blocks);
                } catch (Exception exc) {
                    try {
                        return ListExprParser.parseListExpr(block, pos, blocks);
                    } catch (Exception excp) {
                        throw new ParsingException("This is no expression we can parse.");
                    }
                }
            }
        }
    }

    static ArrayNode getExprArrayAtPos(JsonNode inputs, int pos) {
        List<Map.Entry> slotEntries = new LinkedList<>();
        inputs.fields().forEachRemaining(slotEntries::add);
        Map.Entry slotEntry = slotEntries.get(pos);
        ArrayNode exprArray = (ArrayNode) slotEntry.getValue();
        String numberName = (String) slotEntry
            .getKey(); // we don't need that here but maybe later for storing additional information
        return exprArray;
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    static ArrayNode getDataArrayAtPos(JsonNode inputs, int pos) { // TODO maybe rename or comment
        return (ArrayNode) getExprArrayAtPos(inputs, pos).get(POS_DATA_ARRAY);
    }
}