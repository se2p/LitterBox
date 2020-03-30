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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouches;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ExpressionContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.parseExpression;

public class BoolExprParser {

    public static BoolExpr parseBoolExpr(JsonNode block, String inputName, JsonNode blocks) throws ParsingException {
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
        } else {
            BoolExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }

        throw new ParsingException("Could not parse BoolExpr");
    }

    public static BoolExpr parseBoolExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        int shadowIndicator = ExpressionParser.getShadowIndicator(exprArray);
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                || (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            try {
                return parseBool(block.get(INPUTS_KEY), pos);
            } catch (ParsingException e) {
                return new UnspecifiedBoolExpr();
            }
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            return parseBlockBoolExpr(blocks.get(identifier), blocks);
        } else if (shadowIndicator == INPUT_DIFF_BLOCK_SHADOW
                && exprArray.get(POS_DATA_ARRAY) instanceof NullNode) {
            return new UnspecifiedBoolExpr();
        } else {
            BoolExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }

        throw new ParsingException("Could not parse BoolExpr");
    }


    private static BoolExpr parseVariable(ArrayNode exprArray) {
        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
            VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

            return new AsBool(
                    new Qualified(
                            new StrId(variableInfo.getActor()),
                            new StrId((variableInfo.getVariableName()))
                    ));

        } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
            return new AsBool(
                    new Qualified(
                            new StrId(variableInfo.getActor()),
                            new StrId((variableInfo.getVariableName()))
                    ));
        }
        return null;
    }

    private static BoolLiteral parseBool(JsonNode inputs, int pos) throws ParsingException {
        boolean value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asBoolean();
        return new BoolLiteral(value);
    }

    private static BoolLiteral parseBool(JsonNode inputs, String inputName) throws ParsingException {
        boolean value = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asBoolean();
        return new BoolLiteral(value);
    }

    static Optional<BoolExpr> maybeParseBlockBoolExpr(JsonNode expressionBlock, JsonNode blocks) {
        try {
            return Optional.of(parseBlockBoolExpr(expressionBlock, blocks));
        } catch (ParsingException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static BoolExpr parseBlockBoolExpr(JsonNode expressionBlock, JsonNode blocks)
            throws ParsingException {
        final String opcodeString = expressionBlock.get(OPCODE_KEY).asText();
        if (opcodeString.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            return parseParameter(blocks, expressionBlock);
        }
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString) || opcodeString.equals(StringExprOpcode.data_itemoflist.name()) || opcodeString.equals(NumExprOpcode.data_itemnumoflist.name()), opcodeString + " is not a BoolExprOpcode.");
        if (opcodeString.equals(StringExprOpcode.data_itemoflist.name())) {
            NumExpr index = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            Variable var = ListExprParser.parseVariableFromFields(expressionBlock.get(FIELDS_KEY));
            return new AsBool(new ItemOfVariable(index, var));
        } else if (opcodeString.equals(NumExprOpcode.data_itemnumoflist.name())) {
            Expression item = parseExpression(expressionBlock, 0, blocks);
            Variable list = ListExprParser.parseVariableFromFields(expressionBlock.get(FIELDS_KEY));
            return new AsBool(new IndexOf(item, list));
        }

        final BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);

        switch (opcode) {

        case sensing_touchingcolor:
        case sensing_touchingobject:
            Touchable touchable = TouchableParser.parseTouchable(expressionBlock, blocks);
            return new Touching(touchable);
        case sensing_coloristouchingcolor:
            Color one = ColorParser.parseColor(expressionBlock, 0, blocks);
            Color two = ColorParser.parseColor(expressionBlock, 1, blocks);
            return new ColorTouches(one, two);
        case sensing_keypressed:
            Key key = KeyParser.parse(expressionBlock, blocks);
            return new IsKeyPressed(key);
        case sensing_mousedown:
            return new IsMouseDown();
        case operator_gt:
            ComparableExpr first = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            ComparableExpr second = NumExprParser.parseNumExpr(expressionBlock, 1, blocks);
            if (first instanceof AsNumber) {
                first = new AsString(((AsNumber) first).getOperand1());
            } else if (first instanceof UnspecifiedNumExpr) {
                first = StringExprParser.parseStringExpr(expressionBlock, 0, blocks);
            }

            if (second instanceof AsNumber) {
                second = new AsString(((AsNumber) second).getOperand1());
            } else if (second instanceof UnspecifiedNumExpr) {
                second = StringExprParser.parseStringExpr(expressionBlock, 1, blocks);
            }

            return new BiggerThan(first, second);
        case operator_lt:
            first = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            second = NumExprParser.parseNumExpr(expressionBlock, 1, blocks);
            if (first instanceof AsNumber) {
                first = new AsString(((AsNumber) first).getOperand1());
            } else if (first instanceof UnspecifiedNumExpr) {
                first = StringExprParser.parseStringExpr(expressionBlock, 0, blocks);
            }

            if (second instanceof AsNumber) {
                second = new AsString(((AsNumber) second).getOperand1());
            } else if (second instanceof UnspecifiedNumExpr) {
                second = StringExprParser.parseStringExpr(expressionBlock, 1, blocks);
            }

            return new LessThan(first, second);
        case operator_equals:
            first = NumExprParser.parseNumExpr(expressionBlock, OPERAND1_KEY, blocks);
            second = NumExprParser.parseNumExpr(expressionBlock, OPERAND2_KEY, blocks);
            if (first instanceof AsNumber) {
                first = new AsString(((AsNumber) first).getOperand1());
            } else if (first instanceof UnspecifiedNumExpr) {
                first = StringExprParser.parseStringExpr(expressionBlock, 0, blocks);
            }

            if (second instanceof AsNumber) {
                second = new AsString(((AsNumber) second).getOperand1());
            } else if (second instanceof UnspecifiedNumExpr) {
                second = StringExprParser.parseStringExpr(expressionBlock, 1, blocks);
            }

            return new Equals(first, second);
        case operator_and:

            BoolExpr andFirst = parseCondition(expressionBlock, OPERAND1_KEY, blocks);
            BoolExpr andSecond = parseCondition(expressionBlock, OPERAND2_KEY, blocks);
            return new And(andFirst, andSecond);
        case operator_or:
            BoolExpr orFirst = parseCondition(expressionBlock, OPERAND1_KEY, blocks);
            BoolExpr orSecond = parseCondition(expressionBlock, OPERAND2_KEY, blocks);
            return new Or(orFirst, orSecond);
        case operator_not:
            BoolExpr notInput = parseCondition(expressionBlock, OPERAND_KEY, blocks);
            return new Not(notInput);
        case operator_contains:
            Expression containing = ExpressionParser.parseExpression(expressionBlock, 0, blocks);
            Expression contained = ExpressionParser.parseExpression(expressionBlock, 1, blocks);
            return new ExpressionContains(containing, contained);
        case data_listcontainsitem:
            String identifier =
                    expressionBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
            Variable containingVar;
            if (ProgramParser.symbolTable.getLists().containsKey(identifier)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(identifier);
                containingVar = new Qualified(new StrId(variableInfo.getActor()),
                        new StrId((variableInfo.getVariableName())));
            } else {
                containingVar = new UnspecifiedId();
            }
            contained = ExpressionParser.parseExpression(expressionBlock, 0, blocks);
            return new ExpressionContains(containingVar, contained);
        default:
            throw new RuntimeException(
                    opcodeString + " is not covered by parseBlockExpr");
        }
    }

    private static BoolExpr parseParameter(JsonNode blocks, JsonNode expressionBlock) {
        String name = expressionBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).asText();
        return new AsBool(new StrId(PARAMETER_ABBREVIATION + name));
    }

    private static BoolExpr parseCondition(JsonNode expressionBlock, String fieldName, JsonNode blocks) throws ParsingException {
        if (expressionBlock.get(INPUTS_KEY).has(fieldName)) {
            return parseBoolExpr(expressionBlock, fieldName, blocks);
        } else {
            return new UnspecifiedBoolExpr();
        }
    }
}
