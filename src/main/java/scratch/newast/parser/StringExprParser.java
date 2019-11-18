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
package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_BLOCK_ID;
import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_ID;
import static scratch.newast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.AsString;
import scratch.newast.model.expression.string.AttributeOf;
import scratch.newast.model.expression.string.ItemOfVariable;
import scratch.newast.model.expression.string.Join;
import scratch.newast.model.expression.string.LetterOf;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.expression.string.Username;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.model.variable.Variable;
import scratch.newast.opcodes.StringExprOpcode;
import scratch.newast.parser.symboltable.ExpressionListInfo;
import scratch.newast.parser.symboltable.VariableInfo;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_BLOCK_ID;
import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_ID;
import static scratch.newast.Constants.POS_INPUT_VALUE;

public class StringExprParser {

    public static StringExpr parseStringExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseStr(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            try {
                return parseBlockStringExpr(blocks.get(identifier), blocks);
            } catch (Exception e) {
                try {
                    return new AsString(NumExprParser.parseBlockNumExpr(blocks.get(identifier), blocks));
                } catch (Exception ex) {
                    try {
                        return new AsString(BoolExprParser.parseBlockBoolExpr(blocks.get(identifier), blocks));
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

    private static Str parseStr(JsonNode inputs, int pos) {
        String value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        return new Str(value);
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
            StringExpr attribute = new Str(opcodeString);
            return new AttributeOf(attribute, ActorDefinitionParser.getCurrentActor()); // TODO introduce a mapping opcode -> nicer string
        case sensing_of:
            String prop = expressionBlock.get(FIELDS_KEY).get("PROPERTY").get(0).asText();
            Str property = new Str(prop);
            String menuIdentifier = expressionBlock.get(INPUTS_KEY).get("OBJECT").get(1).asText();
            JsonNode objectMenuBlock = blocks.get(menuIdentifier);
            Identifier identifier = new Identifier(objectMenuBlock.get(FIELDS_KEY).get("OBJECT").get(0).asText()); // TODO introduce constants here
            return new AttributeOf(property, identifier);
        default:
            throw new RuntimeException(opcodeString + " is not covered by parseBlockStringExpr");
        }
    }
}
