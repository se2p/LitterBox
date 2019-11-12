package scratch.newast.parser;

import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_BLOCK_ID;
import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_ID;
import static scratch.newast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.Add;
import scratch.newast.model.expression.num.Current;
import scratch.newast.model.expression.num.DaysSince2000;
import scratch.newast.model.expression.num.DistanceTo;
import scratch.newast.model.expression.num.Div;
import scratch.newast.model.expression.num.IndexOf;
import scratch.newast.model.expression.num.Loudness;
import scratch.newast.model.expression.num.Minus;
import scratch.newast.model.expression.num.Mod;
import scratch.newast.model.expression.num.MouseX;
import scratch.newast.model.expression.num.MouseY;
import scratch.newast.model.expression.num.Mult;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.NumFunctOf;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.num.PickRandom;
import scratch.newast.model.expression.num.Round;
import scratch.newast.model.expression.num.Timer;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.numfunct.Abs;
import scratch.newast.model.numfunct.Acos;
import scratch.newast.model.numfunct.Asin;
import scratch.newast.model.numfunct.Atan;
import scratch.newast.model.numfunct.Ceiling;
import scratch.newast.model.numfunct.Cos;
import scratch.newast.model.numfunct.Floor;
import scratch.newast.model.numfunct.Ln;
import scratch.newast.model.numfunct.Log;
import scratch.newast.model.numfunct.NumFunct;
import scratch.newast.model.numfunct.Pow10;
import scratch.newast.model.numfunct.PowE;
import scratch.newast.model.numfunct.Sin;
import scratch.newast.model.numfunct.Sqrt;
import scratch.newast.model.numfunct.Tan;
import scratch.newast.model.position.Position;
import scratch.newast.model.timecomp.TimeComp;
import scratch.newast.model.variable.Variable;
import scratch.newast.opcodes.NumExprOpcode;

public class ExpressionParser {

    private static final String OPERATOR_KEY = "OPERATOR";

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
            try {
                return parseNumber(inputsNode, pos);
            } catch (NumberFormatException e) {
                throw new ParsingException("There was no parsable float but we didn't implement a solution yet.");
            }
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
        // FIXME TODO you have to differentiate between LengthOfString and LengthOfVar here
        case operator_length:
        case data_lengthoflist:
            // FIXME TODO
        case sensing_current:
            TimeComp timeComp = null; // TODO parse TimeComp
            return new Current(timeComp);
        case sensing_distanceto:
            Position pos = null; // TODO parse position
            return new DistanceTo(pos);
        case operator_add:
            return buildNumExprWithTwoNumExprInputs(Add.class, identifier, blocks);
        case operator_subtract:
            return buildNumExprWithTwoNumExprInputs(Minus.class, identifier, blocks);
        case operator_multiply:
            return buildNumExprWithTwoNumExprInputs(Mult.class, identifier, blocks);
        case operator_divide:
            return buildNumExprWithTwoNumExprInputs(Div.class, identifier, blocks);
        case operator_mod:
            return buildNumExprWithTwoNumExprInputs(Mod.class, identifier, blocks);
        case operator_random:
            return buildNumExprWithTwoNumExprInputs(PickRandom.class, identifier, blocks);
        case operator_mathop:
            NumFunct funct = null; // TODO call parseNumFunct as soon as we have the fields in here
            NumExpr numExpr = parseNumExpr(blocks.get(identifier).get(INPUTS_KEY), 0, blocks);
            return new NumFunctOf(funct, numExpr);
        case data_itemnumoflist:
            Expression item = parseExpression(blocks.get(identifier).get(INPUTS_KEY), 0, blocks);
            Variable list = null; // TODO parse - note that the list is in the "fields" node.
            return new IndexOf(item, list);
        default:
            throw new ParsingException(opcodeString + " not implemented yet");
        }
    }

    public static NumFunct parseNumFunct(JsonNode fields) throws ParsingException { // TODO maybe add opcodes enum for NumFuncts
        ArrayNode operator = (ArrayNode) fields.get(OPERATOR_KEY); // TODO move operator key to suitable place
        String operatorOpcode = operator.get(0).asText(); //TODO remove magic num
        switch (operatorOpcode) {
        case "abs":
            return new Abs();
        case "floor":
            return new Floor();
        case "ceiling":
            return new Ceiling();
        case "sqrt":
            return new Sqrt();
        case "sin":
            return new Sin();
        case "cos":
            return new Cos();
        case "tan":
            return new Tan();
        case "asin":
            return new Asin();
        case "acos":
            return new Acos();
        case "atan":
            return new Atan();
        case "ln":
            return new Ln();
        case "log":
            return new Log();
        case "e ^":
            return new PowE();
        case "10 ^":
            return new Pow10();
        default:
            throw new ParsingException("There is no NumFunct with opcode " + operatorOpcode);
        }
    }

    private static <T extends NumExpr> NumExpr buildNumExprWithTwoNumExprInputs(Class<T> clazz, String identifier, JsonNode blocks) throws ParsingException {
        JsonNode inputs = blocks.get(identifier).get(INPUTS_KEY);
        NumExpr first = parseNumExpr(inputs, 0, blocks);
        NumExpr second = parseNumExpr(inputs, 1, blocks);
        try {
            return (T) clazz.getConstructor(NumExpr.class, NumExpr.class).newInstance(first, second);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParsingException(e);
        }
    }


    public static Expression parseExpression(JsonNode inputsNode, int pos, JsonNode blocks) {
        return null;
    }

    public static StringExpr parseStringExpr(JsonNode inputsNode, int pos, JsonNode blocks) {
        throw new RuntimeException("Not implemented yet");
    }

    public static BoolExpr parseBoolExpr(JsonNode inputsNode, int pos, JsonNode blocks) {
        throw new RuntimeException("Not implemented yet");
    }
    public static BoolExpr parseBoolExpr(JsonNode blocksNode, JsonNode allNodes) {

        throw new RuntimeException("Not implemented yet");
    }

    // (partly wrong) assumption: there is no use case in litterbox where an expression is not inside of the "inputs" part of a block.
    // Well, yes but no: the operator of a NumFunct is in "fields".
}