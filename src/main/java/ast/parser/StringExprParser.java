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

import static ast.Constants.*;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Optional;
import ast.ParsingException;
import ast.model.expression.bool.BoolExpr;
import ast.model.expression.num.NumExpr;
import ast.model.expression.string.*;
import ast.model.literals.StringLiteral;
import ast.model.variable.Identifier;
import ast.model.variable.Qualified;
import ast.model.variable.StrId;
import ast.model.variable.Variable;
import ast.opcodes.ProcedureOpcode;
import ast.opcodes.StringExprOpcode;
import ast.parser.symboltable.ExpressionListInfo;
import ast.parser.symboltable.VariableInfo;
import utils.Preconditions;

public class StringExprParser {

    public static StringExpr parseStringExpr(JsonNode block, String inputName, JsonNode blocks)
            throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayByName(block.get(INPUTS_KEY), inputName);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            try {
                return parseStr(block.get(INPUTS_KEY), inputName);
            } catch (ParsingException e) {
                return new UnspecifiedStringExpr();
            }
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
            try {
                return parseStr(block.get(INPUTS_KEY), pos);
            } catch (ParsingException e) {
                return new UnspecifiedStringExpr();
            }
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
        if (opcode.equals(ProcedureOpcode.argument_reporter_string_number.name()) || opcode.equals(ProcedureOpcode.argument_reporter_boolean.name())) {
            return parseParameter(blocks, exprArray);
        }
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

    private static StringExpr parseParameter(JsonNode blocks, ArrayNode exprArray) {
        JsonNode paramBlock = blocks.get(exprArray.get(POS_BLOCK_ID).asText());
        String name = paramBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).asText();

        return new StrId(PARAMETER_ABBREVIATION + name);
    }

    private static StringLiteral parseStr(JsonNode inputs, int pos) throws ParsingException {
        String value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asText();
        return new StringLiteral(value);
    }

    private static StringLiteral parseStr(JsonNode inputs, String inputName) throws ParsingException {
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
            case looks_costumenumbername: // todo introduce attribute name opcode mapping
                String number_name = expressionBlock.get(FIELDS_KEY).get("NUMBER_NAME").get(0).asText();
                StringLiteral nn_property = new StringLiteral("costume_" + number_name);
                return new AttributeOf(nn_property, ActorDefinitionParser.getCurrentActor());
            case looks_backdropnumbername: // todo introduce attribute name opcode mapping
                number_name = expressionBlock.get(FIELDS_KEY).get("NUMBER_NAME").get(0).asText();
                nn_property = new StringLiteral("backdrop_" + number_name);
                return new AttributeOf(nn_property, ActorDefinitionParser.getCurrentActor());
            case sound_volume:
            case motion_xposition:
            case motion_yposition:
            case motion_direction:
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

                Identifier identifier;
                if (objectMenuBlock != null) {
                    identifier = new StrId(
                            objectMenuBlock.get(FIELDS_KEY).get("OBJECT").get(FIELD_VALUE)
                                    .asText()); // TODO introduce constants here
                } else {
                    //Technically there could be blocks in here, but we do not allow
                    //any expressions to work as identifiers here.
                    identifier = new StrId("");
                }
                return new AttributeOf(property, identifier);
            default:
                throw new RuntimeException(opcodeString + " is not covered by parseBlockStringExpr");
        }
    }
}
