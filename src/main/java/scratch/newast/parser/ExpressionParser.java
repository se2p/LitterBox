package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.DaysSince2000;
import scratch.newast.model.expression.num.Loudness;
import scratch.newast.model.expression.num.MouseX;
import scratch.newast.model.expression.num.MouseY;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.num.Round;
import scratch.newast.model.expression.num.Timer;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.opcodes.NumExprOpcode;
import scratch.newast.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static scratch.newast.Constants.*;

public class ExpressionParser {

    /**
     * Parses the NumExpr at the given position of the given inputsNode.
     *
     * @param inputsNode The JsonNode holding all inputs of a block.
     * @param pos        The index of the NumExpr in the inputsNode.
     * @param blocks     All blocks of the current entity.
     * @return The NumExpr at the position of the inputsNode.
     */
    public static NumExpr parseNumExpr(JsonNode inputsNode, int pos, JsonNode blocks) throws ParsingException { // we ignored "(" NumExpr ")"
        ArrayNode exprArray = getExprArrayAtPos(inputsNode, pos);
        if (getShadowIndicator(exprArray) == 1) {
            return parseNumber(inputsNode, pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            return parseBlockNumExpr(opcode, identifier, blocks);
        } else if (exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText().endsWith("-my variable")) {
            System.out.println("hooray! it's a variable!");
        } else if (!exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText().endsWith("-my variable")) {
            System.out.println("hooray! it's a list!");
        }
        return null;
    }

    public static ArrayNode getExprArrayAtPos(JsonNode inputs, int pos) {
        List<Map.Entry> slotEntries = new LinkedList<>();
        inputs.fields().forEachRemaining(slotEntries::add);
        Map.Entry slotEntry = slotEntries.get(pos);
        ArrayNode exprArray = (ArrayNode) slotEntry.getValue();
        String numberName = (String) slotEntry.getKey(); // we don't need that here but maybe later for storing additional information

        return exprArray;
    }

    public static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    /**
     * Returns the number at the position in the inputs node.
     * For example, if script is the JsonNode holding all blocks
     * and "EU(l=G6)z8NGlJFcx|fS" is a blockID,
     * you can parse the first input to a Number like this:
     * <p>
     * JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
     * Number result = ExpressionParser.parseNumber(inputs, 0);
     * <p>
     * Note that this method only works if there is a number literal at the
     * given position of the inputs.
     *
     * @param inputs The JsonNode holding all inputs of a block.
     * @param pos    The position of the number to parse in the inputs node.
     * @return A Number holding the value of the literal entered.
     */
    public static Number parseNumber(JsonNode inputs, int pos) {
        String valueString = getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        float value = Float.parseFloat(valueString);
        return new Number(value);
    }

    public static ArrayNode getDataArrayAtPos(JsonNode inputs, int pos) { // TODO maybe rename or comment
        return (ArrayNode) getExprArrayAtPos(inputs, pos).get(POS_DATA_ARRAY);
    }


    public static NumExpr parseBlockNumExpr(String opcodeString, String identifier, JsonNode blocks) throws ParsingException {
        Preconditions.checkArgument(NumExprOpcode.contains(opcodeString), opcodeString + " is not a NumExprOpcode.");
        NumExprOpcode opcode = NumExprOpcode.valueOf(opcodeString);
        switch (opcode) {
        // pure reporters
        case sensing_timer:
            return new Timer();
        case sensing_dayssince2000:
            return new DaysSince2000();
        case sensing_mousex:
            return new MouseX();
        case sensing_mousey:
            return new MouseY();
        case sensing_loudness:
            return new Loudness();
        case operator_round:
            JsonNode block = blocks.get(identifier);
            NumExpr num = parseNumExpr(getDataArrayAtPos(block.get(INPUTS_KEY), 0), 0, blocks);
            return new Round(num);
        // One StringExpr or Variable as input
        case operator_length:
        case data_lengthoflist:
            // FIXME TODO
            // One TimeComp as input
        case sensing_current:
            // FIXME TODO
            // one Position as input
        case sensing_distanceto:
            // two NumExpr as inputs
            // FIXME TODO you have to differentiate between LengthOfString and LengthOfVar here
        case operator_add:
            // FIXME TODO
        case operator_subtract:
            // FIXME TODO
        case operator_multiply:
            // FIXME TODO
        case operator_divide:
            // FIXME TODO
        case operator_mod:
            // FIXME TODO
        case operator_random:
            // FIXME TODO
            // NumFunct and NumExpr as inputs
        case operator_mathop:
            // FIXME TODO
            // one Expr and one Variable as inputs
        case data_itemnumoflist:
            // FIXME TODO
            return null;

        default:
            throw new ParsingException(opcodeString + " not implemented yet");
        }
    }

    public static Expression parseExpression(JsonNode inputsNode, int pos, JsonNode blocks) {
        return null;
    }

    public static StringExpr parseStringExpr(JsonNode inputsNode, int pos, JsonNode blocks) {
        return null;
    }

    public static BoolExpr parseBoolExpr(JsonNode blocksNode, JsonNode allNodes) {
        return null;
    }

    // assumption: there is no use case in litterbox where an expression is not inside of the "inputs" part of a block.
}