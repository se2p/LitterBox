package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.Expression;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static scratch.newast.Constants.POS_DATA_ARRAY;

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