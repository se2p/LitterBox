package scratch.newast.parser;

import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.string.StringExpr;

public class ExpressionParser {

    public static Expression parseExpression(String expressionID,
        JsonNode blocks) { // TODO check if these params are sufficient/reasonable
        Expression expression = null;
        return expression;
    }

    public static NumExpr parseNumExpr(String numExprID,
        JsonNode numExprData) { // TODO check if these params are sufficient/reasonable
        NumExpr numExpr = null;
        return numExpr;
    }

    public static NumExpr parseNumExpr(JsonNode node,
        JsonNode numExprData) { // TODO check if these params are sufficient/reasonable
        //This method is here in case we only have an "inputs" array, and not a real block
        NumExpr numExpr = null;
        return numExpr;
    }

    public static BoolExpr parseBoolExpr(
        JsonNode boolExprData, JsonNode allBlocks) { // TODO check if these params are sufficient/reasonable
        BoolExpr boolExpr = null;
        return boolExpr;
    }

    public static BoolExpr parseBoolExpr(String boolExprId,
        JsonNode boolExprData) { // TODO check if these params are sufficient/reasonable
        BoolExpr boolExpr = null;
        return boolExpr;
    }

    public static StringExpr parseStringExpr(String stringExprID,
        JsonNode stringExprData) { // TODO check if these params are sufficient/reasonable
        StringExpr stringExpr = null;
        return stringExpr;
    }

    public static StringExpr parseStringExpr(
        JsonNode stringExprData) { // TODO check if these params are sufficient/reasonable
        StringExpr stringExpr = null;
        return stringExpr;
    }


    /**
     * Returns the number at the position in the inputs node.
     * For example, if script is the JsonNode holding all blocks
     * and "EU(l=G6)z8NGlJFcx|fS" is a blockID,
     * you can parse the first input to a Number like this:
     *
     * JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
     * Number result = ExpressionParser.parseNumber(inputs, 0);
     *
     * Note that this method only works if there is a number literal at the
     * given position of the inputs.
     *
     * @param inputs The JsonNode holding all inputs of a block.
     * @param pos The position of the number to parse in the inputs node.
     * @return A Number holding the value of the literal entered.
     */
    public static Number parseNumber(JsonNode inputs, int pos) {
        List<Map.Entry> slotEntries = new LinkedList<>();
        inputs.fields().forEachRemaining(slotEntries::add);
        Map.Entry slotEntry = slotEntries.get(pos);
        ArrayNode inputArray = (ArrayNode) slotEntry.getValue();
        Number number = new Number(Float.parseFloat(inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asText()));

        String numberName = (String) slotEntry.getKey(); // we don't need that here but maybe later for storing additional information
        return number;
    }

}