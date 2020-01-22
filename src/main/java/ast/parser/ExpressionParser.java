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
package ast.parser;

import static ast.Constants.OPCODE_KEY;
import static ast.Constants.POS_DATA_ARRAY;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ast.Constants;
import ast.ParsingException;
import ast.model.expression.Expression;
import ast.model.expression.UnspecifiedExpression;
import ast.model.expression.bool.BoolExpr;
import ast.model.expression.bool.UnspecifiedBoolExpr;
import ast.model.expression.num.AsNumber;
import ast.model.expression.num.NumExpr;
import ast.model.expression.num.UnspecifiedNumExpr;
import ast.model.expression.string.AsString;
import ast.model.expression.string.StringExpr;
import ast.model.expression.string.UnspecifiedStringExpr;
import ast.model.variable.Qualified;
import ast.model.variable.StrId;
import ast.opcodes.BoolExprOpcode;
import ast.opcodes.NumExprOpcode;
import ast.opcodes.StringExprOpcode;
import ast.parser.symboltable.ExpressionListInfo;
import ast.parser.symboltable.VariableInfo;

public class ExpressionParser {

    public static Expression parseExpression(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        final Optional<NumExpr> numExpr = maybeParseNumExpr(block, pos, blocks);
        if (numExpr.isPresent()
                && !(numExpr.get() instanceof UnspecifiedNumExpr)
                && !(numExpr.get() instanceof AsNumber)) {
            return numExpr.get();
        }

        final Optional<StringExpr> stringExpr = maybeParseStringExpr(block, pos, blocks);
        if (stringExpr.isPresent()
                && !(stringExpr.get() instanceof UnspecifiedStringExpr)
                && !(stringExpr.get() instanceof AsString)) {
            return stringExpr.get();
        }

        final Optional<BoolExpr> boolExpr = maybeParseBoolExpr(block, pos, blocks);
        if (boolExpr.isPresent() && !(boolExpr.get() instanceof UnspecifiedBoolExpr)) {
            return boolExpr.get();
        }

        try {
            return ListExprParser.parseListExpr(block, pos, blocks);
        } catch (Exception excp) {
            return new UnspecifiedExpression();
            //throw new ParsingException("This is no expression we can parse.");
        }
    }

    private static Optional<BoolExpr> maybeParseBoolExpr(JsonNode block, int pos, JsonNode blocks) {
        try {
            final BoolExpr boolExpr = BoolExprParser.parseBoolExpr(block, pos, blocks);
            return Optional.of(boolExpr);
        } catch (ParsingException e) {
            return Optional.empty();
        }
    }

    private static Optional<StringExpr> maybeParseStringExpr(JsonNode block, int pos, JsonNode blocks) {
        try {
            final StringExpr stringExpr = StringExprParser.parseStringExpr(block, pos, blocks);
            return Optional.of(stringExpr);
        } catch (ParsingException e) {
            return Optional.empty();
        }
    }

    private static Optional<NumExpr> maybeParseNumExpr(JsonNode block, int pos, JsonNode blocks) {
        try {
            final NumExpr numExpr = NumExprParser.parseNumExpr(block, pos, blocks);
            return Optional.of(numExpr);
        } catch (ParsingException e) {
            return Optional.empty();
        }
    }

    static ArrayNode getExprArrayAtPos(JsonNode inputs, int pos) {
        List<Map.Entry> slotEntries = new LinkedList<>();
        inputs.fields().forEachRemaining(slotEntries::add);
        Map.Entry slotEntry = slotEntries.get(pos);
        ArrayNode exprArray = (ArrayNode) slotEntry.getValue();
        String numberName = (String) slotEntry
                .getKey(); // we don't need that here but maybe later for storing additional information
        return exprArray;
    }

    static ArrayNode getExprArrayByName(JsonNode inputs, String inputName) {
        return (ArrayNode) inputs.get(inputName);
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    static ArrayNode getDataArrayAtPos(JsonNode inputs, int pos) throws ParsingException { // TODO maybe rename or comment
        JsonNode jsonNode = getExprArrayAtPos(inputs, pos).get(POS_DATA_ARRAY);
        if (!(jsonNode instanceof NullNode)) {
            return (ArrayNode) jsonNode;
        } else {
            throw new ParsingException();
        }
    }

    static ArrayNode getDataArrayByName(JsonNode inputs, String inputName) throws ParsingException { // TODO maybe rename or comment
        final JsonNode jsonNode = getExprArrayByName(inputs, inputName).get(POS_DATA_ARRAY);
        if (!(jsonNode instanceof NullNode)) {
            return (ArrayNode) jsonNode;
        } else {
            throw new ParsingException("Cannot parse null node as ArrayNode");
        }
    }

    public static Expression parseExpressionBlock(JsonNode current, JsonNode allBlocks) throws ParsingException {
        if (current instanceof ArrayNode) {
            // it's a list or variable
            String idString = current.get(2).asText();
            if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
                VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

                return new Qualified(new StrId(variableInfo.getActor()),
                        new StrId((variableInfo.getVariableName())));

            } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
                return new Qualified(new StrId(variableInfo.getActor()),
                        new StrId((variableInfo.getVariableName())));
            }
        } else {
            // it's a normal reporter block
            String opcode = current.get(OPCODE_KEY).asText();
            if (NumExprOpcode.contains(opcode)) {
                return NumExprParser.parseBlockNumExpr(current, allBlocks);
            } else if (StringExprOpcode.contains(opcode)) {
                return StringExprParser.parseBlockStringExpr(current, allBlocks);
            } else if (BoolExprOpcode.contains(opcode)) {
                return BoolExprParser.parseBlockBoolExpr(current, allBlocks);
            } else {
                throw new ParsingException(opcode + " is an unexpected opcode for an expression");
            }
        }
        throw new ParsingException("Calling parseExpressionBlock here went wrong");
    }
}