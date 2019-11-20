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

import static scratch.ast.Constants.FIELD_VALUE;
import static scratch.ast.Constants.INPUTS_KEY;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.Constants.POS_BLOCK_ID;
import static scratch.ast.Constants.POS_DATA_ARRAY;
import static scratch.ast.Constants.POS_INPUT_ID;
import static scratch.ast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import java.util.Optional;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.Key;
import scratch.ast.model.color.Color;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.bool.And;
import scratch.ast.model.expression.bool.BiggerThan;
import scratch.ast.model.expression.bool.Bool;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.ColorTouches;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.bool.ExpressionContains;
import scratch.ast.model.expression.bool.IsKeyPressed;
import scratch.ast.model.expression.bool.IsMouseDown;
import scratch.ast.model.expression.bool.LessThan;
import scratch.ast.model.expression.bool.Not;
import scratch.ast.model.expression.bool.Or;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.touchable.Touchable;
import scratch.ast.model.variable.Qualified;
import scratch.ast.model.variable.StrId;
import scratch.ast.model.variable.Variable;
import scratch.ast.opcodes.BoolExprOpcode;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;

public class BoolExprParser {

    public static BoolExpr parseBoolExpr(JsonNode block, String inputName, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayByName(block.get(INPUTS_KEY), inputName);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseBool(block.get(INPUTS_KEY), inputName);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            return parseBlockBoolExpr(blocks.get(identifier), blocks);
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
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseBool(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            return parseBlockBoolExpr(blocks.get(identifier), blocks);
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

            return new Qualified(new StrId(variableInfo.getActor()),
                new StrId((variableInfo.getVariableName())));

        } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
            ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
            return new Qualified(new StrId(variableInfo.getActor()),
                new StrId((variableInfo.getVariableName())));
        }
        return null;
    }

    private static Bool parseBool(JsonNode inputs, int pos) {
        boolean value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asBoolean();
        return new Bool(value);
    }

    private static Bool parseBool(JsonNode inputs, String inputName) {
        boolean value = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asBoolean();
        return new Bool(value);
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
        String opcodeString = expressionBlock.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString), opcodeString + " is not a BoolExprOpcode.");
        BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);
        switch (opcode) {
        case sensing_touchingcolor:
        case sensing_touchingobject:
            Touchable touchable = TouchableParser.parseTouchable(expressionBlock, blocks);
            return new Touching(touchable);
        case sensing_coloristouchingcolor:
            Color first = ColorParser.parseColor(expressionBlock, 0, blocks);
            Color second = ColorParser.parseColor(expressionBlock, 1, blocks);
            return new ColorTouches(first, second);
        case sensing_keypressed:
            Key key = KeyParser.parse(expressionBlock, blocks);
            return new IsKeyPressed(key);
        case sensing_mousedown:
            return new IsMouseDown();
        case operator_gt:
            NumExpr firstNum = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            NumExpr secondNum = NumExprParser.parseNumExpr(expressionBlock, 1, blocks);
            return new BiggerThan(firstNum, secondNum);
        case operator_lt:
            NumExpr lessFirst = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            NumExpr lessSecond = NumExprParser.parseNumExpr(expressionBlock, 1, blocks);
            return new LessThan(lessFirst, lessSecond);
        case operator_equals:
            NumExpr eqFirst = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
            NumExpr eqSecond = NumExprParser.parseNumExpr(expressionBlock, 1, blocks);
            return new Equals(eqFirst, eqSecond);
        case operator_and:
            BoolExpr andFirst = parseBoolExpr(expressionBlock, 0, blocks);
            BoolExpr andSecond = parseBoolExpr(expressionBlock, 1, blocks);
            return new And(andFirst, andSecond);
        case operator_or:
            BoolExpr orFirst = parseBoolExpr(expressionBlock, 0, blocks);
            BoolExpr orSecond = parseBoolExpr(expressionBlock, 1, blocks);
            return new Or(orFirst, orSecond);
        case operator_not:
            BoolExpr notInput = parseBoolExpr(expressionBlock, 0, blocks);
            return new Not(notInput);
        case operator_contains:
            Expression containing = ExpressionParser.parseExpression(expressionBlock, 0, blocks);
            Expression contained = ExpressionParser.parseExpression(expressionBlock, 1, blocks);
            return new ExpressionContains(containing, contained);
        case data_listcontainsitem:
            String listName = expressionBlock.get(Constants.FIELDS_KEY).get("LIST").get(FIELD_VALUE).asText();
            Variable containingVar = new StrId(listName);// Variable as a ListExpr
            contained = ExpressionParser.parseExpression(expressionBlock, 0, blocks);
            return new ExpressionContains(containingVar, contained);


        default:
            throw new RuntimeException(
                    opcodeString + " is not covered by parseBlockExpr");
        }
    }
}
