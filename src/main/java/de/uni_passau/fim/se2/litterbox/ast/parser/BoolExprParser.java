/*
 * Copyright (C) 2020 LitterBox contributors
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
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BoolExprParser {

    /**
     * Returns true iff the input of the containing block is parsable as BoolExpr,
     * excluding as BoolLiteral, as these are a theoretical construct and would
     * be parsed as StringExpr when the ExpressionParser is called.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the the input of the containing block is parsable as BoolExpr.
     */
    public static boolean parsableAsBoolExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks) {
        ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
        boolean hasBoolExprOpcode = false;
        JsonNode input = exprArray.get(POS_BLOCK_ID);
        if (input instanceof TextNode) {
            String identifier = input.asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            String opcodeString = exprBlock.get(OPCODE_KEY).asText();
            hasBoolExprOpcode = BoolExprOpcode.contains(opcodeString);
        } else if (input instanceof NullNode) {
            return true;
        }
        return hasBoolExprOpcode;
    }

    /**
     * Parses the input of the containingBlock specified by the inputKey.
     * If the input does not contain a BoolExpr calls the ExpressionParser
     * and wraps the result as BoolExpr.
     *
     * @param containingBlock The block inputs of which contain the expression to be parsed.
     * @param inputKey        The key of the input which contains the expression.
     * @param allBlocks       All blocks of the actor definition currently parsed.
     * @return The expression identified by the inputKey.
     * @throws ParsingException If parsing fails.
     */
    public static BoolExpr parseBoolExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks)
            throws ParsingException {

        if (parsableAsBoolExpr(containingBlock, inputKey, allBlocks)) {
            ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
            if (exprArray == null || exprArray.get(POS_INPUT_VALUE) instanceof NullNode) {
                return new UnspecifiedBoolExpr();
            }
            int shadowIndicator = ExpressionParser.getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                    || (shadowIndicator == INPUT_BLOCK_NO_SHADOW
                    && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseBool(containingBlock.get(INPUTS_KEY), inputKey);
                } catch (ParsingException e) {
                    return new UnspecifiedBoolExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockBoolExpr(identifier, allBlocks.get(identifier), allBlocks);
            } else if (shadowIndicator == INPUT_DIFF_BLOCK_SHADOW
                    && exprArray.get(POS_DATA_ARRAY) instanceof NullNode) {
                return new UnspecifiedBoolExpr();
            }
        } else {
            return new AsBool(ExpressionParser.parseExpr(containingBlock, inputKey, allBlocks));
        }

        throw new ParsingException("Could not parse BoolExpr");
    }

    /**
     * Parses a single BoolExpr corresponding to a reporter block.
     * The opcode of the block has to be a BoolExprOpcode.
     *
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no NumExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static BoolExpr parseBlockBoolExpr(String blockId, JsonNode exprBlock, JsonNode allBlocks)
            throws ParsingException {
        final String opcodeString = exprBlock.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString),
                        opcodeString + " is not a BoolExprOpcode.");
        final BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, exprBlock);
        switch (opcode) {

            case sensing_touchingcolor:
                Touchable color = TouchableParser.parseTouchable(exprBlock, allBlocks);
                return new SpriteTouchingColor(color, metadata);
            case sensing_touchingobject:
                Touchable touchable = TouchableParser.parseTouchable(exprBlock, allBlocks);
                return new Touching(touchable, metadata);
            case sensing_coloristouchingcolor:
                Color one = ColorParser.parseColor(exprBlock, COLOR_KEY, allBlocks);
                Color two = ColorParser.parseColor(exprBlock, COLOR2_KEY, allBlocks);
                return new ColorTouchingColor(one, two, metadata);
            case sensing_keypressed:
                Key key = KeyParser.parse(exprBlock, allBlocks);
                return new IsKeyPressed(key, metadata);
            case sensing_mousedown:
                return new IsMouseDown(metadata);
            case operator_gt:
                ComparableExpr first = NumExprParser.parseNumExpr(exprBlock, OPERAND1_KEY, allBlocks);
                ComparableExpr second = NumExprParser.parseNumExpr(exprBlock, OPERAND2_KEY, allBlocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExpr(exprBlock, OPERAND1_KEY, allBlocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExpr(exprBlock, OPERAND2_KEY, allBlocks);
                }

                return new BiggerThan(first, second, metadata);
            case operator_lt:
                first = NumExprParser.parseNumExpr(exprBlock, OPERAND1_KEY, allBlocks);
                second = NumExprParser.parseNumExpr(exprBlock, OPERAND2_KEY, allBlocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExpr(exprBlock, OPERAND1_KEY, allBlocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExpr(exprBlock, OPERAND2_KEY, allBlocks);
                }

                return new LessThan(first, second, metadata);
            case operator_equals:
                first = NumExprParser.parseNumExpr(exprBlock, OPERAND1_KEY, allBlocks);
                second = NumExprParser.parseNumExpr(exprBlock, OPERAND2_KEY, allBlocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExpr(exprBlock, OPERAND1_KEY, allBlocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExpr(exprBlock, OPERAND2_KEY, allBlocks);
                }

                return new Equals(first, second, metadata);
            case operator_and:
                BoolExpr andFirst = parseCondition(exprBlock, OPERAND1_KEY, allBlocks);
                BoolExpr andSecond = parseCondition(exprBlock, OPERAND2_KEY, allBlocks);
                return new And(andFirst, andSecond, metadata);
            case operator_or:
                BoolExpr orFirst = parseCondition(exprBlock, OPERAND1_KEY, allBlocks);
                BoolExpr orSecond = parseCondition(exprBlock, OPERAND2_KEY, allBlocks);
                return new Or(orFirst, orSecond, metadata);
            case operator_not:
                BoolExpr notInput = parseCondition(exprBlock, OPERAND_KEY, allBlocks);
                return new Not(notInput, metadata);
            case operator_contains:
                StringExpr containing = StringExprParser.parseStringExpr(exprBlock, STRING1_KEY, allBlocks);
                StringExpr contained = StringExprParser.parseStringExpr(exprBlock, STRING2_KEY, allBlocks);
                return new StringContains(containing, contained, metadata);
            case data_listcontainsitem:
                String identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                String listName =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                Identifier containingVar;
                String currentActorName = ActorDefinitionParser.getCurrentActor().getName();
                Optional<ExpressionListInfo> list
                        = ProgramParser.symbolTable.getList(identifier, listName, currentActorName);

                if (list.isPresent()) {
                    ExpressionListInfo variableInfo = list.get();
                    containingVar = new Qualified(new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId((variableInfo.getVariableName()))));
                } else {
                    containingVar = new UnspecifiedId();
                }
                contained = StringExprParser.parseStringExpr(exprBlock, ITEM_KEY, allBlocks);
                return new ListContains(containingVar, contained, metadata);
            default:
                throw new RuntimeException(
                        opcodeString + " is not covered by parseBlockExpr");
        }
    }

    private static BoolLiteral parseBool(JsonNode inputs, String inputKey) throws ParsingException {
        boolean value = ExpressionParser.getDataArrayByName(inputs, inputKey).get(POS_INPUT_VALUE).asBoolean();
        return new BoolLiteral(value);
    }

    /**
     * As in Scratch there are no default boolean values
     * the input can be empty and is then returned as UnspecifiedBoolExpr - directly calling parseBoolExpr()
     * would result in a ParsingException.
     */
    private static BoolExpr parseCondition(JsonNode exprBlock, String fieldName, JsonNode allBlocks)
            throws ParsingException {

        if (exprBlock.get(INPUTS_KEY).has(fieldName)) {
            return parseBoolExpr(exprBlock, fieldName, allBlocks);
        } else {
            return new UnspecifiedBoolExpr();
        }
    }
}
