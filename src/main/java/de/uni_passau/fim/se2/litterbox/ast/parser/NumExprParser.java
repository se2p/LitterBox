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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.lang.reflect.InvocationTargetException;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.*;

public class NumExprParser {

    /**
     * Returns true if the input of the containing block is parsable as NumExpr,
     * excluding casts with AsNumber.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputName       The name of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the the in put of the containing block is parsable as NumExpr.
     * @throws ParsingException If the json is malformed.
     */
    @SuppressWarnings("unused")
    public static boolean parsableAsNumExpr(JsonNode containingBlock,
                                            String inputName, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = containingBlock.get(INPUTS_KEY);
        ArrayNode exprArray = getExprArrayByName(inputs, inputName);
        int shadowIndicator = getShadowIndicator(exprArray);

        // parsable as NumberLiteral
        boolean parsableAsNumberLiteral = false;
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW ||
                (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            try {
                String valueString = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asText();
                float value = Float.parseFloat(valueString);
                parsableAsNumberLiteral = true;
            } catch (NumberFormatException e) {
                // not parsable as NumberLiteral
            }
        }

        // or NumExpr opcode
        boolean hasNumExprOpcode = false;
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            JsonNode opcodeNode = exprBlock.get(OPCODE_KEY);
            String opcodeString = opcodeNode.asText();
            hasNumExprOpcode = NumExprOpcode.contains(opcodeString);
        }
        return hasNumExprOpcode || parsableAsNumberLiteral;
    }

    public static NumExpr parseNumExprWithName(JsonNode containingBlock, String inputName, JsonNode allBlocks)
            throws ParsingException {
        if (parsableAsNumExpr(containingBlock, inputName, allBlocks)) {

            ArrayNode exprArray = getExprArrayByName(containingBlock.get(INPUTS_KEY), inputName);
            int shadowIndicator = getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW ||
                    (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseNumber(containingBlock.get(INPUTS_KEY), inputName);
                } catch (NumberFormatException | ParsingException e) {
                    return new UnspecifiedNumExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockNumExpr(allBlocks.get(identifier), allBlocks);
            }
        } else {
            return new AsNumber(ExpressionParser.parseExprWithName(containingBlock, inputName, allBlocks));
        }
        throw new ParsingException("Could not parse NumExpr.");
    }

    /**
     * FIXME this is the old version of parsing with position instead of input name. adjust.
     * Returns the number at the position in the inputs node. For example, if script is the JsonNode holding all blocks
     * and "EU(l=G6)z8NGlJFcx|fS" is a blockID, you can parse the first input to a Number like this:
     * <p>
     * JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs"); Number result =
     * ExpressionParser.parseNumber(inputs, 0);
     * <p>
     * Note that this method only works if there is a number literal at the given position of the inputs.
     *
     * @param inputs    The JsonNode holding all inputs of a block.
     * @param inputName The name of the input to parse in the inputs node.
     * @return A Number holding the value of the literal entered.
     */
    static NumberLiteral parseNumber(JsonNode inputs, String inputName) throws ParsingException {
        String valueString = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asText();
        float value = Float.parseFloat(valueString);
        return new NumberLiteral(value);
    }

    /**
     * Parses a single NumExpression corresponding to a reporter block.
     * The opcode of the block has to be a NumExprOpcode.
     *
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks The JsonNode holding all allBlocks of the program.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no NumExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static NumExpr parseBlockNumExpr(JsonNode exprBlock, JsonNode allBlocks)
            throws ParsingException {
        String opcodeString = exprBlock.get(OPCODE_KEY).asText();
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
                NumExpr num = parseNumExprWithName(exprBlock, NUM_KEY, allBlocks);
                return new Round(num);
            case operator_length:
                return new LengthOfString(StringExprParser.parseStringExprWithName(exprBlock, STRING_KEY, allBlocks));
            case data_lengthoflist:
                String identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
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
                TimeComp timeComp = TimecompParser.parse(exprBlock);
                return new Current(timeComp);
            case sensing_distanceto:
                Position pos = PositionParser.parse(exprBlock, allBlocks);
                return new DistanceTo(pos);
            case operator_add:
                return buildNumExprWithTwoNumExprInputs(Add.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks);
            case operator_subtract:
                return buildNumExprWithTwoNumExprInputs(Minus.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks);
            case operator_multiply:
                return buildNumExprWithTwoNumExprInputs(Mult.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks);
            case operator_divide:
                return buildNumExprWithTwoNumExprInputs(Div.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks);
            case operator_mod:
                return buildNumExprWithTwoNumExprInputs(Mod.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks);
            case operator_random:
                return buildNumExprWithTwoNumExprInputs(PickRandom.class, exprBlock, FROM_KEY, TO_KEY, allBlocks);
            case operator_mathop:
                NumFunct funct = parseNumFunct(exprBlock.get(FIELDS_KEY));
                NumExpr numExpr = parseNumExprWithName(exprBlock, NUM_KEY, allBlocks);
                return new NumFunctOf(funct, numExpr);
            case data_itemnumoflist:
                Expression item = parseExprWithName(exprBlock, ITEM_KEY, allBlocks);
                identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
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
     * @param clazz     The class implementing NumExpr of which an instance is to be created
     * @param exprBlock The JsonNode of the NumExpr
     * @param blocks    The script of which the Expression which is to pe parsed is part of
     * @param <T>       A class which has to implement the {@link NumExpr} interface
     * @return A new T instance holding the right two NumExpr inputs
     * @throws ParsingException If creating the new T instance goes wrong
     */
    private static <T extends NumExpr> NumExpr buildNumExprWithTwoNumExprInputs(Class<T> clazz,
                                                                                JsonNode exprBlock,
                                                                                String firstInputName,
                                                                                String secondInputName,
                                                                                JsonNode blocks) throws ParsingException {
        NumExpr first = parseNumExprWithName(exprBlock, firstInputName, blocks);
        NumExpr second = parseNumExprWithName(exprBlock, secondInputName, blocks);
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
