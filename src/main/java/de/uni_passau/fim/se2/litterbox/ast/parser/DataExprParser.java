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

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode.argument_reporter_boolean;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode.argument_reporter_string_number;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser.symbolTable;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class DataExprParser {

    /**
     * Returns true iff the input of the containing block is a Variable, ScratchList or
     * Parameter.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the the input of the containing block is parsable as DataExpr.
     */
    public static boolean parsableAsDataExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks) {
        ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return true; // it is a DataExpr
            } else {
                String opcode = exprBlock.get(OPCODE_KEY).asText();
                return opcode.equals(argument_reporter_string_number.name())
                        || opcode.equals(argument_reporter_boolean.name());
            }
        } else if (exprArray.get(POS_DATA_ARRAY) instanceof ArrayNode) {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            //return symbolTable.getVariables().containsKey(idString)
            //        || symbolTable.getLists().containsKey(idString);
            return true; // the above is the "strict" truth, but some JSON files
            // contain references to IDs which are not present in the lookup tables
            // and we want to keep these without exception
        }
        return false;
    }

    /**
     * Parses the DataExpr of the input of the block.
     *
     * @param containingBlock The block the input of which contains a DataExpr.
     * @param inputKey        Key of the input holding the DataExpr.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return The DataExpr - either a Parameter, Variable or ScratchList.
     */
    public static Expression parseDataExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkArgument(parsableAsDataExpr(containingBlock, inputKey, allBlocks));
        ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = allBlocks.get(identifier).get(OPCODE_KEY).asText();
            boolean isNumOrStringParam = opcode.equals(argument_reporter_string_number.name());
            boolean isBooleanParam = opcode.equals(argument_reporter_boolean.name());
            boolean isParameter = isNumOrStringParam || isBooleanParam;
            if (isParameter) {
                return parseParameter(exprArray, allBlocks);
            }
        } else if (exprArray.get(POS_DATA_ARRAY) instanceof ArrayNode) {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            boolean isVariable = symbolTable.getVariables().containsKey(idString);
            boolean isList = symbolTable.getLists().containsKey(idString);
            if (isVariable) {
                return parseVariable(exprArray);
            } else if (isList) {
                return parseScratchList(exprArray);
            } else {
                return new UnspecifiedId();
            }
        }
        throw new IllegalArgumentException("The block does not contain a DataExpr.");
    }

    /**
     * Parses the ScratchList stored in the expression array.
     *
     * @param exprArray The expression array containing the ScratchList.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @return The ScratchList wrapped as Qualified.
     */
    private static Parameter parseParameter(ArrayNode
                                                    exprArray, JsonNode allBlocks) throws ParsingException {
        JsonNode paramBlock = allBlocks.get(exprArray.get(POS_BLOCK_ID).asText());
        String name = paramBlock.get(FIELDS_KEY).get(VALUE_KEY).get(VARIABLE_NAME_POS).asText();
        BlockMetadata metadata = BlockMetadataParser.parse(exprArray.get(POS_BLOCK_ID).asText(), paramBlock);
        return new Parameter(new StrId(name), metadata);
    }

    /**
     * Parses the ScratchList stored in the expression array.
     *
     * @param exprArray The expression array containing the ScratchList.
     * @return The ScratchList wrapped as Qualified.
     */
    private static Qualified parseScratchList(ArrayNode exprArray) {
        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        Preconditions.checkArgument(symbolTable.getLists().containsKey(idString));
        ExpressionListInfo variableInfo = symbolTable.getLists().get(idString);
        return new Qualified(
                new StrId(variableInfo.getActor()),
                new ScratchList(new StrId((variableInfo.getVariableName()))));
    }

    /**
     * Parses the Variable stored in the expression array.
     *
     * @param exprArray The expression array containing the Variable.
     * @return The Variable wrapped as Qualified.
     */
    private static Qualified parseVariable(ArrayNode exprArray) {
        String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
        Preconditions.checkArgument(symbolTable.getVariables().containsKey(idString));
        VariableInfo variableInfo = symbolTable.getVariables().get(idString);
        return new Qualified(
                new StrId(variableInfo.getActor()),
                new Variable(new StrId((variableInfo.getVariableName()))));
    }
}