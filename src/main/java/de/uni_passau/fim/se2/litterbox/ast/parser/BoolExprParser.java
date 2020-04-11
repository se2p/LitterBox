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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BoolExprParser {

    /**
     * Returns true iff the input of the containing block is parsable as BoolExpr,
     * excluding as BoolLiteral, as these are a theoretical construct and would
     * be parsed as StringExpr when the ExpressionParser is called.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputName       The name of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the the in put of the containing block is parsable as BoolExpr.
     */
    public static boolean parsableAsBoolExpr(JsonNode containingBlock, String inputName, JsonNode allBlocks) {
        ArrayNode exprArray = ExpressionParser.getExprArrayByName(containingBlock.get(INPUTS_KEY), inputName);
        boolean hasBoolExprOpcode = false;
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            String opcodeString = exprBlock.get(OPCODE_KEY).asText();
            hasBoolExprOpcode = BoolExprOpcode.contains(opcodeString);
        }
        return hasBoolExprOpcode;
    }

    public static BoolExpr parseBoolExprWithName(JsonNode block, String inputName, JsonNode blocks) throws ParsingException {
        if (parsableAsBoolExpr(block, inputName, blocks)) {
            ArrayNode exprArray = ExpressionParser.getExprArrayByName(block.get(INPUTS_KEY), inputName);
            if (exprArray == null) {
                return new UnspecifiedBoolExpr();
            }
            int shadowIndicator = ExpressionParser.getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                    || (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseBool(block.get(INPUTS_KEY), inputName);
                } catch (ParsingException e) {
                    return new UnspecifiedBoolExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockBoolExpr(blocks.get(identifier), blocks);
            } else if (shadowIndicator == INPUT_DIFF_BLOCK_SHADOW
                    && exprArray.get(POS_DATA_ARRAY) instanceof NullNode) {
                return new UnspecifiedBoolExpr();
            }
        } else {
            return new AsBool(ExpressionParser.parseExprWithName(block, inputName, blocks));
        }

        throw new ParsingException("Could not parse BoolExpr");
    }

    private static BoolLiteral parseBool(JsonNode inputs, String inputName) throws ParsingException {
        boolean value = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asBoolean();
        return new BoolLiteral(value);
    }

    static BoolExpr parseBlockBoolExpr(JsonNode exprBlock, JsonNode blocks)
            throws ParsingException {
        final String opcodeString = exprBlock.get(OPCODE_KEY).asText();
        if (opcodeString.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            return parseParameter(exprBlock);
        }
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString),
                        opcodeString + " is not a BoolExprOpcode.");
        final BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);

        switch (opcode) {

            case sensing_touchingcolor:
                Touchable color = TouchableParser.parseTouchable(exprBlock, blocks);
                return new SpriteTouchingColor(color);
            case sensing_touchingobject:
                Touchable touchable = TouchableParser.parseTouchable(exprBlock, blocks);
                return new Touching(touchable);
            case sensing_coloristouchingcolor:
                Color one = ColorParser.parseColor(exprBlock, COLOR_KEY, blocks);
                Color two = ColorParser.parseColor(exprBlock, COLOR2_KEY, blocks);
                return new ColorTouchingColor(one, two);
            case sensing_keypressed:
                Key key = KeyParser.parse(exprBlock, blocks);
                return new IsKeyPressed(key);
            case sensing_mousedown:
                return new IsMouseDown();
            case operator_gt:
                ComparableExpr first = NumExprParser.parseNumExprWithName(exprBlock, OPERAND1_KEY, blocks);
                ComparableExpr second = NumExprParser.parseNumExprWithName(exprBlock, OPERAND2_KEY, blocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExprWithName(exprBlock, OPERAND1_KEY, blocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExprWithName(exprBlock, OPERAND2_KEY, blocks);
                }

                return new BiggerThan(first, second);
            case operator_lt:
                first = NumExprParser.parseNumExprWithName(exprBlock, OPERAND1_KEY, blocks);
                second = NumExprParser.parseNumExprWithName(exprBlock, OPERAND2_KEY, blocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExprWithName(exprBlock, OPERAND1_KEY, blocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExprWithName(exprBlock, OPERAND2_KEY, blocks);
                }

                return new LessThan(first, second);
            case operator_equals:
                first = NumExprParser.parseNumExprWithName(exprBlock, OPERAND1_KEY, blocks);
                second = NumExprParser.parseNumExprWithName(exprBlock, OPERAND2_KEY, blocks);
                if (first instanceof AsNumber) {
                    if (((AsNumber) first).getOperand1() instanceof StringExpr) {
                        first = (StringExpr) ((AsNumber) first).getOperand1();
                    } else {
                        first = new AsString(((AsNumber) first).getOperand1());
                    }
                } else if (first instanceof UnspecifiedNumExpr) {
                    first = StringExprParser.parseStringExprWithName(exprBlock, OPERAND1_KEY, blocks);
                }

                if (second instanceof AsNumber) {
                    if (((AsNumber) second).getOperand1() instanceof StringExpr) {
                        second = (StringExpr) ((AsNumber) second).getOperand1();
                    } else {
                        second = new AsString(((AsNumber) second).getOperand1());
                    }
                } else if (second instanceof UnspecifiedNumExpr) {
                    second = StringExprParser.parseStringExprWithName(exprBlock, OPERAND2_KEY, blocks);
                }

                return new Equals(first, second);
            case operator_and:
                BoolExpr andFirst = parseCondition(exprBlock, OPERAND1_KEY, blocks);
                BoolExpr andSecond = parseCondition(exprBlock, OPERAND2_KEY, blocks);
                return new And(andFirst, andSecond);
            case operator_or:
                BoolExpr orFirst = parseCondition(exprBlock, OPERAND1_KEY, blocks);
                BoolExpr orSecond = parseCondition(exprBlock, OPERAND2_KEY, blocks);
                return new Or(orFirst, orSecond);
            case operator_not:
                BoolExpr notInput = parseCondition(exprBlock, OPERAND_KEY, blocks);
                return new Not(notInput);
            case operator_contains:
                StringExpr containing = StringExprParser.parseStringExprWithName(exprBlock, STRING1_KEY, blocks);
                StringExpr contained = StringExprParser.parseStringExprWithName(exprBlock, STRING2_KEY, blocks);
                return new StringContains(containing, contained);
            case data_listcontainsitem:
                String identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                Identifier containingVar;
                if (ProgramParser.symbolTable.getLists().containsKey(identifier)) {
                    ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(identifier);
                    containingVar = new Qualified(new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId((variableInfo.getVariableName()))));
                } else {
                    containingVar = new UnspecifiedId();
                }
                contained = StringExprParser.parseStringExprWithName(exprBlock, ITEM_KEY, blocks);
                return new ListContains(containingVar, contained);
            default:
                throw new RuntimeException(
                        opcodeString + " is not covered by parseBlockExpr");
        }
    }

    private static BoolExpr parseParameter(JsonNode exprBlock) {
        String name = exprBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).asText();
        return new AsBool(new Parameter(new StrId(name)));
    }

    private static BoolExpr parseCondition(JsonNode exprBlock, String fieldName, JsonNode blocks) throws ParsingException {
        if (exprBlock.get(INPUTS_KEY).has(fieldName)) {
            return parseBoolExprWithName(exprBlock, fieldName, blocks);
        } else {
            return new UnspecifiedBoolExpr();
        }
    }
}
