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

import static scratch.ast.Constants.*;
import static scratch.ast.parser.ExpressionParser.getDataArrayAtPos;
import static scratch.ast.parser.ExpressionParser.getExprArrayAtPos;
import static scratch.ast.parser.ExpressionParser.getExprArrayByName;
import static scratch.ast.parser.ExpressionParser.getShadowIndicator;
import static scratch.ast.parser.ExpressionParser.parseExpression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import scratch.ast.ParsingException;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.num.Add;
import scratch.ast.model.expression.num.AsNumber;
import scratch.ast.model.expression.num.Current;
import scratch.ast.model.expression.num.DaysSince2000;
import scratch.ast.model.expression.num.DistanceTo;
import scratch.ast.model.expression.num.Div;
import scratch.ast.model.expression.num.IndexOf;
import scratch.ast.model.expression.num.LengthOfString;
import scratch.ast.model.expression.num.LengthOfVar;
import scratch.ast.model.expression.num.Loudness;
import scratch.ast.model.expression.num.Minus;
import scratch.ast.model.expression.num.Mod;
import scratch.ast.model.expression.num.MouseX;
import scratch.ast.model.expression.num.MouseY;
import scratch.ast.model.expression.num.Mult;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.num.NumFunct;
import scratch.ast.model.expression.num.NumFunctOf;
import scratch.ast.model.expression.num.PickRandom;
import scratch.ast.model.expression.num.Round;
import scratch.ast.model.expression.num.Timer;
import scratch.ast.model.expression.num.UnspecifiedNumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.position.Position;
import scratch.ast.model.procedure.Parameter;
import scratch.ast.model.timecomp.TimeComp;
import scratch.ast.model.type.BooleanType;
import scratch.ast.model.type.StringType;
import scratch.ast.model.variable.Qualified;
import scratch.ast.model.variable.StrId;
import scratch.ast.model.variable.Variable;
import scratch.ast.opcodes.NumExprOpcode;
import scratch.ast.opcodes.ProcedureOpcode;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;
import utils.Preconditions;

public class NumExprParser {

    public static NumExpr parseNumExpr(JsonNode block, String inputName, JsonNode blocks)
            throws ParsingException { // we ignored "(" NumExpr ")"
        ArrayNode exprArray = getExprArrayByName(block.get(INPUTS_KEY), inputName);
        if (getShadowIndicator(exprArray) == 1) { // TODO replace magic num
            try {
                return parseNumber(block.get(INPUTS_KEY), inputName);
            } catch (NumberFormatException e) { // right exception? hm.
                return new UnspecifiedNumExpr();
            }

        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return parseTextNode(blocks, exprArray);

        } else {
            NumExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }
        throw new ParsingException("Could not parse NumExpr.");
    }

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
            } catch (NumberFormatException e) {
                return new UnspecifiedNumExpr();
            }

        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return parseTextNode(blocks, exprArray);

        } else {
            NumExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }
        throw new ParsingException("Could not parse NumExpr.");
    }

    private static NumExpr parseTextNode(JsonNode blocks, ArrayNode exprArray) throws ParsingException {
        String identifier = exprArray.get(POS_BLOCK_ID).asText();
        String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
        if (opcode.equals(ProcedureOpcode.argument_reporter_string_number.name()) || opcode.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            return parseParameter(blocks, exprArray);
        }
        final Optional<NumExpr> optExpr = maybeParseBlockNumExpr(blocks.get(identifier), blocks);
        if (optExpr.isPresent()) {
            return optExpr.get();
        }

        final Optional<StringExpr> stringExpr = StringExprParser
                .maybeParseBlockStringExpr(blocks.get(identifier), blocks);
        if (stringExpr.isPresent()) {
            return new AsNumber(stringExpr.get());
        }

        final Optional<BoolExpr> boolExpr = BoolExprParser.maybeParseBlockBoolExpr(blocks.get(identifier), blocks);
        if (boolExpr.isPresent()) {
            return new AsNumber(boolExpr.get());
        }

        throw new ParsingException(
                "Could not parse NumExpr for block with id " + identifier + " and opcode " + opcode);
    }

    private static NumExpr parseParameter(JsonNode blocks, ArrayNode exprArray) {
        JsonNode paramBlock = blocks.get(exprArray.get(POS_BLOCK_ID).textValue());
        String name = paramBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).textValue();
        return new AsNumber(new StrId(PARAMETER_ABBREVIATION + name));
    }


    private static NumExpr parseVariable(ArrayNode exprArray) {
        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
            VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

            return new Qualified(new StrId(variableInfo.getActor()),
                    new StrId((variableInfo.getVariableName())));

        } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
            return new Qualified(new StrId(variableInfo.getActor()),
                    new StrId((variableInfo.getVariableName())));
        }
        return null;
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
    static NumberLiteral parseNumber(JsonNode inputs, int pos) {
        String valueString = getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        float value = Float.parseFloat(valueString);
        return new NumberLiteral(value);
    }

    static NumberLiteral parseNumber(JsonNode inputs, String inputName) {
        String valueString = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asText();
        float value = Float.parseFloat(valueString);
        return new NumberLiteral(value);
    }

    static Optional<NumExpr> maybeParseBlockNumExpr(JsonNode expressionBlock, JsonNode blocks) {
        try {
            return Optional.of(parseBlockNumExpr(expressionBlock, blocks));
        } catch (ParsingException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static NumExpr parseBlockNumExpr(JsonNode expressionBlock, JsonNode blocks)
            throws ParsingException {
        String opcodeString = expressionBlock.get(OPCODE_KEY).asText();
        Preconditions.checkArgument(NumExprOpcode.contains(opcodeString), opcodeString + " is not a NumExprOpcode.");
        NumExprOpcode opcode = NumExprOpcode.valueOf(opcodeString);
        switch (opcode) {
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
                NumExpr num = parseNumExpr(expressionBlock, 0, blocks);
                return new Round(num);
            case operator_length:
                return new LengthOfString(StringExprParser.parseStringExpr(expressionBlock, 0, blocks));
            case data_lengthoflist:
                return new LengthOfVar(
                        new StrId(expressionBlock.get(FIELDS_KEY).get(LIST_NAME_POS).textValue()));
            case sensing_current:
                TimeComp timeComp = TimecompParser.parse(expressionBlock);
                return new Current(timeComp);
            case sensing_distanceto:
                Position pos = PositionParser.parse(expressionBlock, blocks);
                return new DistanceTo(pos);
            case operator_add:
                return buildNumExprWithTwoNumExprInputs(Add.class, expressionBlock, blocks);
            case operator_subtract:
                return buildNumExprWithTwoNumExprInputs(Minus.class, expressionBlock, blocks);
            case operator_multiply:
                return buildNumExprWithTwoNumExprInputs(Mult.class, expressionBlock, blocks);
            case operator_divide:
                return buildNumExprWithTwoNumExprInputs(Div.class, expressionBlock, blocks);
            case operator_mod:
                return buildNumExprWithTwoNumExprInputs(Mod.class, expressionBlock, blocks);
            case operator_random:
                return buildNumExprWithTwoNumExprInputs(PickRandom.class, expressionBlock, blocks);
            case operator_mathop:
                NumFunct funct = parseNumFunct(expressionBlock.get(FIELDS_KEY));
                NumExpr numExpr = parseNumExpr(expressionBlock, 0, blocks);
                return new NumFunctOf(funct, numExpr);
            case data_itemnumoflist:
                Expression item = parseExpression(expressionBlock, 0, blocks);
                Variable list = ListExprParser.parseVariableFromFields(expressionBlock.get(FIELDS_KEY));
                return new IndexOf(item, list);
            default:
                throw new ParsingException(opcodeString + " is not covered by parseBlockNumExpr");
        }
    }

    /**
     * Parses the inputs of the NumExpr the identifier of which is handed over and returns the NumExpr holding its two
     * inputs.
     *
     * @param clazz           The class implementing NumExpr of which an instance is to be created
     * @param expressionBlock The JsonNode of the NumExpr
     * @param blocks          The script of which the Expression which is to pe parsed is part of
     * @param <T>             A class which has to implement the {@link NumExpr} interface
     * @return A new T instance holding the right two NumExpr inputs
     * @throws ParsingException If creating the new T instance goes wrong
     */
    private static <T extends NumExpr> NumExpr buildNumExprWithTwoNumExprInputs(Class<T> clazz,
                                                                                JsonNode expressionBlock,
                                                                                JsonNode blocks) throws ParsingException {
        NumExpr first = parseNumExpr(expressionBlock, 0, blocks);
        NumExpr second = parseNumExpr(expressionBlock, 1, blocks);
        try {
            return clazz.getConstructor(NumExpr.class, NumExpr.class).newInstance(first, second);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParsingException(e);
        }
    }

    static NumFunct parseNumFunct(JsonNode fields)
            throws ParsingException { // TODO maybe add opcodes enum for NumFuncts
        ArrayNode operator = (ArrayNode) fields.get(OPERATOR_KEY);
        String operatorOpcode = operator.get(FIELD_VALUE).asText();
        return NumFunct.fromString(operatorOpcode);
    }
}
