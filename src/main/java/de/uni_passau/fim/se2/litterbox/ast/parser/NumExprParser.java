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
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.*;

public class NumExprParser {

    public static NumExpr parseNumExpr(JsonNode block, String inputName, JsonNode blocks)
            throws ParsingException {
        ArrayNode exprArray = getExprArrayByName(block.get(INPUTS_KEY), inputName);
        int shadowIndicator = getShadowIndicator(exprArray);
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW ||
                (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            try {
                return parseNumber(block.get(INPUTS_KEY), inputName);
            } catch (NumberFormatException | ParsingException e) { // right exception? hm.
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
        int shadowIndicator = getShadowIndicator(exprArray);
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW ||
                (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            try {
                return parseNumber(block.get(INPUTS_KEY), pos);
            } catch (NumberFormatException | ParsingException e) {
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

        return new UnspecifiedNumExpr();
    }

    private static NumExpr parseParameter(JsonNode blocks, ArrayNode exprArray) {
        JsonNode paramBlock = blocks.get(exprArray.get(POS_BLOCK_ID).asText());
        String name = paramBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).asText();
        return new AsNumber(new Parameter(new StrId(name)));
    }

    private static NumExpr parseVariable(ArrayNode exprArray) {
        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
            VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

            return new AsNumber(
                    new Qualified(
                            new StrId(variableInfo.getActor()),
                            new Variable(new StrId((variableInfo.getVariableName()))
                            )
                    ));
        } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
            return new AsNumber(
                    new Qualified(
                            new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId((variableInfo.getVariableName())
                            ))
                    ));
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
    static NumberLiteral parseNumber(JsonNode inputs, int pos) throws ParsingException {
        String valueString = getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        float value = Float.parseFloat(valueString);
        return new NumberLiteral(value);
    }

    static NumberLiteral parseNumber(JsonNode inputs, String inputName) throws ParsingException {
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

    /**
     * Parses a single NumExpression corresponding to a reporter block.
     * The opcode of the block has to be a NumExprOpcode.
     *
     * @param expressionBlock The JsonNode of the reporter block.
     * @param allBlocks       The JsonNode holding all allBlocks of the program.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no NumExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static NumExpr parseBlockNumExpr(JsonNode expressionBlock, JsonNode allBlocks)
            throws ParsingException {
        String opcodeString = expressionBlock.get(OPCODE_KEY).asText();
        Preconditions.checkArgument(NumExprOpcode.contains(opcodeString),
                opcodeString + " is not a NumExprOpcode.");
        NumExprOpcode opcode = NumExprOpcode.valueOf(opcodeString);
        switch (opcode) {
            case sound_volume:
                return new Volume();
            case motion_xposition:
                return new PositionX();
            case motion_yposition:
                return new PositionY();
            case motion_direction:
                return new Direction();
            case looks_size:
                return new Size();
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
                NumExpr num = parseNumExpr(expressionBlock, 0, allBlocks);
                return new Round(num);
            case operator_length:
                return new LengthOfString(StringExprParser.parseStringExpr(expressionBlock, 0, allBlocks));
            case data_lengthoflist:
                String identifier =
                        expressionBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                Identifier var;
                if (ProgramParser.symbolTable.getLists().containsKey(identifier)) {
                    ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(identifier);
                    var = new Qualified(new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId(variableInfo.getVariableName())));
                } else {
                    var = new UnspecifiedId();
                }
                return new LengthOfVar(var);
            case sensing_current:
                TimeComp timeComp = TimecompParser.parse(expressionBlock);
                return new Current(timeComp);
            case sensing_distanceto:
                Position pos = PositionParser.parse(expressionBlock, allBlocks);
                return new DistanceTo(pos);
            case operator_add:
                return buildNumExprWithTwoNumExprInputs(Add.class, expressionBlock, allBlocks);
            case operator_subtract:
                return buildNumExprWithTwoNumExprInputs(Minus.class, expressionBlock, allBlocks);
            case operator_multiply:
                return buildNumExprWithTwoNumExprInputs(Mult.class, expressionBlock, allBlocks);
            case operator_divide:
                return buildNumExprWithTwoNumExprInputs(Div.class, expressionBlock, allBlocks);
            case operator_mod:
                return buildNumExprWithTwoNumExprInputs(Mod.class, expressionBlock, allBlocks);
            case operator_random:
                return buildNumExprWithTwoNumExprInputs(PickRandom.class, expressionBlock, allBlocks);
            case operator_mathop:
                NumFunct funct = parseNumFunct(expressionBlock.get(FIELDS_KEY));
                NumExpr numExpr = parseNumExpr(expressionBlock, 0, allBlocks);
                return new NumFunctOf(funct, numExpr);
            case data_itemnumoflist:
                Expression item = parseExpressionWithPos(expressionBlock, 0, allBlocks);
                identifier =
                        expressionBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                if (ProgramParser.symbolTable.getLists().containsKey(identifier)) {
                    ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(identifier);
                    var = new Qualified(new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId(variableInfo.getVariableName())));
                } else {
                    var = new UnspecifiedId();
                }
                return new IndexOf(item, var);
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

    /**
     * Parses a NumFunct from the "fields" JsonNode.
     * The node has to have an "OPERATOR" key.
     *
     * @param fields The JsonNode containing the operator of the NumFunct.
     * @return The NumFunct stored in the fields node.
     */
    static NumFunct parseNumFunct(JsonNode fields) {
        Preconditions.checkArgument(fields.has(OPERATOR_KEY));
        ArrayNode operator = (ArrayNode) fields.get(OPERATOR_KEY);
        String operatorOpcode = operator.get(FIELD_VALUE).asText();
        return NumFunct.fromString(operatorOpcode);
    }
}
