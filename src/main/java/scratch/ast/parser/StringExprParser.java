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

import static scratch.ast.Constants.FIELDS_KEY;
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
import java.util.Optional;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.string.AsString;
import scratch.ast.model.expression.string.AttributeOf;
import scratch.ast.model.expression.string.ItemOfVariable;
import scratch.ast.model.expression.string.Join;
import scratch.ast.model.expression.string.LetterOf;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.expression.string.Username;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.Qualified;
import scratch.ast.model.variable.StrId;
import scratch.ast.model.variable.Variable;
import scratch.ast.opcodes.StringExprOpcode;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;
import utils.Preconditions;

public class StringExprParser {

    public static StringExpr parseStringExpr(JsonNode block, String inputName, JsonNode blocks)
        throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayByName(block.get(INPUTS_KEY), inputName);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseStr(block.get(INPUTS_KEY), inputName);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return parseTextNode(blocks, exprArray);

        } else {
            StringExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }
        throw new ParsingException("Could not parse StringExpr");
    }

    public static StringExpr parseStringExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseStr(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            return parseTextNode(blocks, exprArray);

        } else {
            StringExpr variableInfo = parseVariable(exprArray);
            if (variableInfo != null) {
                return variableInfo;
            }
        }
        throw new ParsingException("Could not parse StringExpr");
    }


    private static StringExpr parseVariable(ArrayNode exprArray) {
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

    private static StringExpr parseTextNode(JsonNode blocks, ArrayNode exprArray) throws ParsingException {
        String identifier = exprArray.get(POS_BLOCK_ID).asText();
        String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();

        final Optional<StringExpr> stringExpr = maybeParseBlockStringExpr(blocks.get(identifier), blocks);
        if (stringExpr.isPresent()) {
            return stringExpr.get();
        }

        final Optional<NumExpr> optExpr = NumExprParser.maybeParseBlockNumExpr(blocks.get(identifier), blocks);
        if (optExpr.isPresent()) {
            return new AsString(optExpr.get());
        }

        final Optional<BoolExpr> boolExpr = BoolExprParser.maybeParseBlockBoolExpr(blocks.get(identifier), blocks);
        if (boolExpr.isPresent()) {
            return new AsString(boolExpr.get());
        }

        throw new ParsingException(
            "Could not parse NumExpr for block with id " + identifier + " and opcode " + opcode);
    }

    private static StringLiteral parseStr(JsonNode inputs, int pos) {
        String value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        return new StringLiteral(value);
    }

    private static StringLiteral parseStr(JsonNode inputs, String inputName) {
        String value = ExpressionParser.getDataArrayByName(inputs, inputName).get(POS_INPUT_VALUE).asText();
        return new StringLiteral(value);
    }

    static Optional<StringExpr> maybeParseBlockStringExpr(JsonNode expressionBlock, JsonNode blocks) {
        try {
            return Optional.of(parseBlockStringExpr(expressionBlock, blocks));
        } catch (ParsingException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static StringExpr parseBlockStringExpr(JsonNode expressionBlock, JsonNode blocks) throws ParsingException {
        String opcodeString = expressionBlock.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(StringExprOpcode.contains(opcodeString), opcodeString + " is not a StringExprOpcode.");
        StringExprOpcode opcode = StringExprOpcode.valueOf(opcodeString);
        switch (opcode) {
            case operator_join:
                StringExpr first = parseStringExpr(expressionBlock, 0, blocks);
                StringExpr second = parseStringExpr(expressionBlock, 1, blocks);
                return new Join(first, second);
            case operator_letter_of:
                NumExpr num = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
                StringExpr word = parseStringExpr(expressionBlock, 1, blocks);
                return new LetterOf(num, word);
            case sensing_username:
                return new Username();
            case data_itemoflist:
                NumExpr index = NumExprParser.parseNumExpr(expressionBlock, 0, blocks);
                Variable var = ListExprParser.parseVariableFromFields(expressionBlock.get(FIELDS_KEY));
                return new ItemOfVariable(index, var);
            case sound_volume:
            case motion_xposition:
            case motion_yposition:
            case motion_direction:
            case looks_costumenumbername: // has a number_name in fields, what to do? -> enter number or name FIXME
            case looks_backdropnumbername: // has a number_name in fields, what to do? -> num or name FIXME
            case looks_size:
            case sensing_answer:
                StringExpr attribute = new StringLiteral(opcodeString);
                return new AttributeOf(attribute,
                    ActorDefinitionParser.getCurrentActor()); // TODO introduce a mapping opcode -> nicer string
            case sensing_of:
                String prop = expressionBlock.get(FIELDS_KEY).get("PROPERTY").get(0).asText();
                StringLiteral property = new StringLiteral(prop);
                String menuIdentifier = expressionBlock.get(INPUTS_KEY).get("OBJECT").get(1).asText();
                JsonNode objectMenuBlock = blocks.get(menuIdentifier);
                Identifier identifier = new StrId(
                    objectMenuBlock.get(FIELDS_KEY).get("OBJECT").get(FIELD_VALUE)
                        .asText()); // TODO introduce constants here
                return new AttributeOf(property, identifier);
            default:
                throw new RuntimeException(opcodeString + " is not covered by parseBlockStringExpr");
        }
    }
}
