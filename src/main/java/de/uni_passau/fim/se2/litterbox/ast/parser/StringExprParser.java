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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcodes.sensing_of_object_menu;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.getExprArray;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.getShadowIndicator;

public class StringExprParser {

    /**
     * Returns true iff the input of the containing block is parsable as StringExpr,
     * excluding casts with AsString.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the the input of the containing block is parsable as StringExpr.
     */
    public static boolean parsableAsStringExpr(JsonNode containingBlock,
                                               String inputKey, JsonNode allBlocks) {
        JsonNode inputs = containingBlock.get(INPUTS_KEY);
        ArrayNode exprArray = getExprArray(inputs, inputKey);
        int shadowIndicator = getShadowIndicator(exprArray);

        boolean parsableAsStringLiteral = false;
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW ||
                (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            parsableAsStringLiteral = true;
        }

        // or parsable as StringExpr
        boolean hasStringExprOpcode = false;
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            String opcodeString = exprBlock.get(OPCODE_KEY).asText();
            hasStringExprOpcode = StringExprOpcode.contains(opcodeString);
        }
        return hasStringExprOpcode || parsableAsStringLiteral;
    }

    /**
     * Parses the input of the containingBlock specified by the inputKey.
     * If the input does not contain a StringExpr calls the ExpressionParser
     * and wraps the result as StringExpr.
     *
     * @param containingBlock The block inputs of which contain the expression to be parsed.
     * @param inputKey        The key of the input which contains the expression.
     * @param allBlocks       All blocks of the actor definition currently parsed.
     * @return The expression identified by the inputKey.
     * @throws ParsingException If parsing fails.
     */
    public static StringExpr parseStringExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks)
            throws ParsingException {
        if (parsableAsStringExpr(containingBlock, inputKey, allBlocks)) {
            ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
            int shadowIndicator = ExpressionParser.getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                    || (shadowIndicator == INPUT_BLOCK_NO_SHADOW && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseStr(containingBlock.get(INPUTS_KEY), inputKey);
                } catch (ParsingException e) {
                    return new UnspecifiedStringExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockStringExpr(allBlocks.get(identifier), allBlocks);
            }
        } else {
            return new AsString(ExpressionParser.parseExpr(containingBlock, inputKey, allBlocks));
        }
        throw new ParsingException("Could not parse StringExpr");
    }

    /**
     * Parses a single StringExpr corresponding to a reporter block.
     * The opcode of the block has to be a StringExprOpcode.
     *
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no StringExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static StringExpr parseBlockStringExpr(JsonNode exprBlock, JsonNode allBlocks) throws ParsingException {
        String opcodeString = exprBlock.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(StringExprOpcode.contains(opcodeString), opcodeString + " is not a StringExprOpcode.");
        StringExprOpcode opcode = StringExprOpcode.valueOf(opcodeString);
        switch (opcode) {
            case operator_join:
                StringExpr first = parseStringExpr(exprBlock, STRING1_KEY, allBlocks);
                StringExpr second = parseStringExpr(exprBlock, STRING2_KEY, allBlocks);
                return new Join(first, second);
            case operator_letter_of:
                NumExpr num = NumExprParser.parseNumExpr(exprBlock, LETTER_KEY, allBlocks);
                StringExpr word = parseStringExpr(exprBlock, STRING_KEY, allBlocks);
                return new LetterOf(num, word);
            case sensing_username:
                return new Username();
            case data_itemoflist:
                NumExpr index = NumExprParser.parseNumExpr(exprBlock, INDEX_KEY, allBlocks);
                String id =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                Identifier var;
                if (ProgramParser.symbolTable.getLists().containsKey(id)) {
                    ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(id);
                    var = new Qualified(new StrId(variableInfo.getActor()),
                            new ScratchList(new StrId((variableInfo.getVariableName()))));
                } else {
                    var = new UnspecifiedId();
                }
                return new ItemOfVariable(index, var);
            case looks_costumenumbername:
                String number_name = exprBlock.get(FIELDS_KEY).get(NUMBER_NAME_KEY).get(0).asText();
                return new Costume(NameNum.fromString(number_name));
            case looks_backdropnumbername:
                number_name = exprBlock.get(FIELDS_KEY).get(NUMBER_NAME_KEY).get(0).asText();
                return new Backdrop(NameNum.fromString(number_name));
            case sensing_answer:
                return new Answer();
            case sensing_of:
                String menuIdentifier = exprBlock.get(INPUTS_KEY).get(OBJECT_KEY).get(1).asText();
                JsonNode objectMenuBlock = allBlocks.get(menuIdentifier);

                Expression localIdentifier;
                if (objectMenuBlock != null) {
                    JsonNode menuOpcode = objectMenuBlock.get(OPCODE_KEY);
                    if (menuOpcode.asText().equalsIgnoreCase(sensing_of_object_menu.name())) {
                        localIdentifier = new StrId(
                                objectMenuBlock.get(FIELDS_KEY).get(OBJECT_KEY).get(FIELD_VALUE)
                                        .asText());
                    } else {
                        localIdentifier = ExpressionParser.parseExpr(exprBlock, OBJECT_KEY, allBlocks);
                    }
                } else {
                    localIdentifier = new StrId(""); //TODO still correct?
                }
                String prop = exprBlock.get(FIELDS_KEY).get("PROPERTY").get(0).asText();
                Attribute property;
                switch (prop) {
                    case "y position":
                    case "x position":
                    case "direction":
                    case "costume #":
                    case "costume name":
                    case "size":
                    case "volume":
                    case "backdrop name":
                    case "backdrop #":
                        property = new AttributeFromFixed(FixedAttribute.fromString(prop));
                        break;
                    default:
                        property = new AttributeFromVariable(new StrId(prop));
                }
                return new AttributeOf(property, localIdentifier);
            default:
                throw new RuntimeException(opcodeString + " is not covered by parseBlockStringExpr");
        }
    }

    private static StringLiteral parseStr(JsonNode inputs, String inputKey) throws ParsingException {
        String value = ExpressionParser.getDataArrayByName(inputs, inputKey).get(POS_INPUT_VALUE).asText();
        return new StringLiteral(value);
    }
}
