package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.Key;
import scratch.newast.model.color.Color;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.And;
import scratch.newast.model.expression.bool.BiggerThan;
import scratch.newast.model.expression.bool.Bool;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.ColorTouches;
import scratch.newast.model.expression.bool.Equals;
import scratch.newast.model.expression.bool.IsKeyPressed;
import scratch.newast.model.expression.bool.IsMouseDown;
import scratch.newast.model.expression.bool.LessThan;
import scratch.newast.model.expression.bool.Not;
import scratch.newast.model.expression.bool.Or;
import scratch.newast.model.expression.bool.Touching;
import scratch.newast.model.expression.list.ListExpr;
import scratch.newast.model.expression.num.Add;
import scratch.newast.model.expression.num.AsNumber;
import scratch.newast.model.expression.num.Current;
import scratch.newast.model.expression.num.DaysSince2000;
import scratch.newast.model.expression.num.DistanceTo;
import scratch.newast.model.expression.num.Div;
import scratch.newast.model.expression.num.IndexOf;
import scratch.newast.model.expression.num.LengthOfString;
import scratch.newast.model.expression.num.LengthOfVar;
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
import scratch.newast.model.expression.string.AsString;
import scratch.newast.model.expression.string.ItemOfVariable;
import scratch.newast.model.expression.string.Join;
import scratch.newast.model.expression.string.LetterOf;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.expression.string.Username;
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
import scratch.newast.model.touchable.Touchable;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.model.variable.Variable;
import scratch.newast.opcodes.BoolExprOpcode;
import scratch.newast.opcodes.CallStmtOpcode;
import scratch.newast.opcodes.NumExprOpcode;
import scratch.newast.opcodes.StringExprOpcode;
import scratch.newast.parser.symboltable.ExpressionListInfo;
import scratch.newast.parser.symboltable.VariableInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.LIST_NAME_POS;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_BLOCK_ID;
import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_ID;
import static scratch.newast.Constants.POS_INPUT_VALUE;

public class ExpressionParser {

    private static final String OPERATOR_KEY = "OPERATOR";

    /**
     * Parses the NumExpr at the given position of the given block.
     *
     * @param block  The JsonNode holding the block of which a NumExpr has to be parsed.
     * @param pos    The index of the NumExpr in the block.
     * @param blocks All blocks of the current entity.
     * @return The NumExpr at the position of the block.
     */
    public static NumExpr parseNumExpr(JsonNode block, int pos, JsonNode blocks)
            throws ParsingException { // we ignored "(" NumExpr ")"
        ArrayNode exprArray = getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (getShadowIndicator(exprArray) == 1) { // TODO replace magic num
            try {
                return parseNumber(block.get(INPUTS_KEY), pos);
            } catch (NumberFormatException e) { // right exception? hm.
                throw new ParsingException("There was no parsable float but we didn't implement a solution yet.");
            }

        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            try {
                return parseBlockNumExpr(opcode, identifier, blocks, block.get(FIELDS_KEY));
            } catch (Exception e) {
                try {
                    return new AsNumber(parseBlockStringExpr(opcode, identifier, blocks, block.get(FIELDS_KEY)));
                } catch (Exception ex) {
                    try {
                        return new AsNumber(parseBlockBoolExpr(opcode, identifier, blocks, block.get(FIELDS_KEY)));
                    } catch (Exception exc) {
                        throw new ParsingException(exc);
                    }
                }
            }
        } else {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
                VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));

            } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));
            }
        }
        throw new ParsingException("Could not parse NumExpr.");
    }

    public static ArrayNode getExprArrayAtPos(JsonNode inputs, int pos) {
        List<Map.Entry> slotEntries = new LinkedList<>();
        inputs.fields().forEachRemaining(slotEntries::add);
        Map.Entry slotEntry = slotEntries.get(pos);
        ArrayNode exprArray = (ArrayNode) slotEntry.getValue();
        String numberName = (String) slotEntry
                .getKey(); // we don't need that here but maybe later for storing additional information
        return exprArray;
    }

    public static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    /**
     * Returns the number at the position in the inputs node. For example, if script is the JsonNode holding all blocks
     * and "EU(l=G6)z8NGlJFcx|fS" is a blockID, you can parse the first input to a Number like this:
     * <p>
     * JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs"); Number result =
     * ExpressionParser.parseNumber(inputs, 0);
     * <p>
     * Note that this method only works if there is a number literal at the given position of the inputs.
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

    public static Str parseStr(JsonNode inputs, int pos) {
        String value = getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        return new Str(value);
    }

    public static Bool parseBool(JsonNode inputs, int pos) {
        boolean value = getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asBoolean();
        return new Bool(value);
    }

    public static ArrayNode getDataArrayAtPos(JsonNode inputs, int pos) { // TODO maybe rename or comment
        return (ArrayNode) getExprArrayAtPos(inputs, pos).get(POS_DATA_ARRAY);
    }

    public static NumExpr parseBlockNumExpr(String opcodeString, String identifier, JsonNode blocks, JsonNode fields)
            throws ParsingException {
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
            NumExpr num = parseNumExpr(block, 0, blocks);
            return new Round(num);
        // One StringExpr or Variable as input
        case operator_length:
            return new LengthOfString(parseStringExpr(blocks.get(identifier), 0, blocks));
        case data_lengthoflist:
            return new LengthOfVar(
                    new Identifier(blocks.get(identifier).get(FIELDS_KEY).get(LIST_NAME_POS).textValue()));
        case sensing_current:
            TimeComp timeComp = TimecompParser.parse(blocks.get(identifier));
            return new Current(timeComp);
        case sensing_distanceto:
            Position pos = PositionParser.parse(blocks.get(identifier), blocks);
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
            NumFunct funct = parseNumFunct(fields);
            NumExpr numExpr = parseNumExpr(blocks.get(identifier), 0, blocks);
            return new NumFunctOf(funct, numExpr);
        case data_itemnumoflist:
            Expression item = parseExpression(blocks.get(identifier).get(INPUTS_KEY), 0, blocks);
            Variable list = parseVariable();
            return new IndexOf(item, list);
        default:
            throw new ParsingException(opcodeString + " not implemented yet");
        }
    }


    private static StringExpr parseBlockStringExpr(String opcodeString, String identifier, JsonNode blocks,
                                                   JsonNode fields) throws ParsingException {
        Preconditions
                .checkArgument(StringExprOpcode.contains(opcodeString), opcodeString + " is not a StringExprOpcode.");
        StringExprOpcode opcode = StringExprOpcode.valueOf(opcodeString);
        switch (opcode) {
        case operator_join:
            StringExpr first = parseStringExpr(blocks.get(identifier), 0, blocks);
            StringExpr second = parseStringExpr(blocks.get(identifier), 1, blocks);
            return new Join(first, second);
        case operator_letter_of:
            NumExpr num = parseNumExpr(blocks.get(identifier), 0, blocks);
            StringExpr word = parseStringExpr(blocks.get(identifier), 1, blocks);
            return new LetterOf(num, word);
        case sensing_username:
            return new Username();
        case data_itemoflist:
            NumExpr index = parseNumExpr(blocks.get(identifier), 0, blocks);
            Variable var = parseVariable();
            return new ItemOfVariable(index, var);
        default:
            throw new RuntimeException(
                    opcodeString + " not implemented yet or this method was not called properly (or JSON is wrong)");
        }
    }

    private static BoolExpr parseBlockBoolExpr(String opcodeString, String identifier, JsonNode blocks, JsonNode fields)
            throws ParsingException {
        Preconditions
            .checkArgument(BoolExprOpcode.contains(opcodeString), opcodeString + " is not a BoolExprOpcode.");
        BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);
        switch (opcode) {
        case sensing_touchingobject:
            Touchable touchable = TouchableParser.parseTouchable(blocks.get(identifier), blocks);
            return new Touching(touchable);
        case sensing_touchingcolor:
            //TODO
            throw new RuntimeException("Not implemented yet");
        case sensing_coloristouchingcolor:
            Color first = ColorParser.parseColor(blocks.get(identifier), 0, blocks);
            Color second = ColorParser.parseColor(blocks.get(identifier), 1, blocks);
            return new ColorTouches(first, second);
        case sensing_keypressed:
            Key key = KeyParser.parse(blocks.get(identifier), blocks);
            return new IsKeyPressed(key);
        case sensing_mousedown:
            return new IsMouseDown();
        case operator_gt:
            NumExpr firstNum = parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr secondNum = parseNumExpr(blocks.get(identifier), 1, blocks);
            return new BiggerThan(firstNum, secondNum);
        case operator_lt:
            NumExpr lessFirst = parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr lessSecond = parseNumExpr(blocks.get(identifier), 1, blocks);
            return new LessThan(lessFirst, lessSecond);
        case operator_equals:
            NumExpr eqFirst = parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr eqSecond = parseNumExpr(blocks.get(identifier), 1, blocks);
            return new Equals(eqFirst, eqSecond);
        case operator_and:
            BoolExpr andFirst = parseBoolExpr(blocks.get(identifier), 0, blocks);
            BoolExpr andSecond = parseBoolExpr(blocks.get(identifier), 1, blocks);
            return new And(andFirst, andSecond);
        case operator_or:
            BoolExpr orFirst = parseBoolExpr(blocks.get(identifier), 0, blocks);
            BoolExpr orSecond = parseBoolExpr(blocks.get(identifier), 1, blocks);
            return new Or(orFirst, orSecond);
        case operator_not:
            BoolExpr notInput = parseBoolExpr(blocks.get(identifier), 0, blocks);
            return new Not(notInput);
        case operator_contains:
        case data_listcontainsitem:
            throw new RuntimeException("Not implemented yet"); // I don't know which Classes should be returned here
        default:
            throw new RuntimeException(
                    opcodeString + " not implemented yet or this method was not called properly (or JSON is wrong)");
        }
    }

    public static NumFunct parseNumFunct(JsonNode fields)
            throws ParsingException { // TODO maybe add opcodes enum for NumFuncts
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

    private static <T extends NumExpr> NumExpr buildNumExprWithTwoNumExprInputs(Class<T> clazz, String identifier,
                                                                                JsonNode blocks) throws ParsingException {
        JsonNode block = blocks.get(identifier);
        NumExpr first = parseNumExpr(block, 0, blocks);
        NumExpr second = parseNumExpr(block, 1, blocks);
        try {
            return (T) clazz.getConstructor(NumExpr.class, NumExpr.class).newInstance(first, second);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParsingException(e);
        }
    }

    public static Expression parseExpression(JsonNode block, int pos, JsonNode blocks) {
        String opcodeString = block.get(OPCODE_KEY).asText();
        if (CallStmtOpcode.contains(opcodeString)) {
            //Get Argumenttypes from ProcedureDefinitionNameMapping
            throw new RuntimeException("Not implemented yet");
        }

        throw new RuntimeException("Not implemented yet");
    }

    public static StringExpr parseStringExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (getShadowIndicator(exprArray) == 1) {
            return parseStr(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            try {
                return parseBlockStringExpr(opcode, identifier, blocks, block.get(FIELDS_KEY));
            } catch (Exception e) {
                try {
                    return new AsString(parseBlockNumExpr(opcode, identifier, blocks, block.get(FIELDS_KEY)));
                } catch (Exception ex) {
                    try {
                        return new AsString(parseBlockBoolExpr(opcode, identifier, blocks, block.get(FIELDS_KEY)));
                    } catch (Exception exc) {
                        throw new ParsingException(exc);
                    }
                }
            }
        } else {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
                VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));

            } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));
            }
        }
        throw new ParsingException("Could not parse StringExpr");
    }

    public static BoolExpr parseBoolExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (getShadowIndicator(exprArray) == 1) {
            return parseBool(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            return parseBlockBoolExpr(opcode, identifier, blocks, block.get(FIELDS_KEY));
        } else {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
                VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));

            } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));
            }
        }

        throw new ParsingException("Not implemented yet");
    }

    public static ListExpr parseListExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = getExprArrayAtPos(block.get(INPUTS_KEY), pos);

        if (getShadowIndicator(exprArray) == 1 || exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            throw new ParsingException("Block does not contain a list"); //Todo improve message
        }

        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
            return new Qualified(new Identifier(variableInfo.getActor()),
                    new Identifier((variableInfo.getVariableName())));
        }

        throw new ParsingException("Block does not contain a list"); //Todo improve message
    }

    private static Variable parseVariable() { // TODO parse - note that the list is in the "fields" node. -- update probably I have to use the lookuptable here in order to distinguish Ident and Ident . Ident
        throw new RuntimeException("Not implemented yet. VARIABLES ARE EVIL BUT DO PARSE THEM");
    }


    // (partly wrong) assumption: there is no use case in litterbox where an expression is not inside of the "inputs" part of a block.
    // Well, yes but no: the operator of a NumFunct is in "fields".
}