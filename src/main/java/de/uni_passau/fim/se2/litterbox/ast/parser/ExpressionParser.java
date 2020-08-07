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
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.StringExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;

import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.POS_DATA_ARRAY;
import static de.uni_passau.fim.se2.litterbox.ast.parser.BoolExprParser.parsableAsBoolExpr;
import static de.uni_passau.fim.se2.litterbox.ast.parser.DataExprParser.parsableAsDataExpr;
import static de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser.parsableAsNumExpr;
import static de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser.parsableAsStringExpr;

public class ExpressionParser {

    /**
     * Parses a expression which is input to another containingBlock. The parsed expression
     * may not directly correspond to a reporter block but can also be a literal,
     * for example.
     *
     * @param containingBlock The block inputs of which contain the expression to be parsed.
     * @param inputKey        The key identifying the expression input.
     * @param allBlocks       All blocks of the actor definition currently parsed.
     * @return The expression identified by the inputKey.
     * @throws ParsingException If parsing fails.
     */
    public static Expression parseExpr(JsonNode containingBlock,
                                       String inputKey,
                                       JsonNode allBlocks)
            throws ParsingException {
        Expression expr = null;
        if (parsableAsNumExpr(containingBlock, inputKey, allBlocks)) {
            expr = NumExprParser.parseNumExpr(containingBlock, inputKey, allBlocks);
        } else if (parsableAsStringExpr(containingBlock, inputKey, allBlocks)) {
            expr = StringExprParser.parseStringExpr(containingBlock, inputKey, allBlocks);
        } else if (parsableAsBoolExpr(containingBlock, inputKey, allBlocks)) {
            expr = BoolExprParser.parseBoolExpr(containingBlock, inputKey, allBlocks);
        } else if (parsableAsDataExpr(containingBlock, inputKey, allBlocks)) {
            expr = DataExprParser.parseDataExpr(containingBlock, inputKey, allBlocks);
        }
        if (expr != null) {
            return expr;
        } else {
            return new UnspecifiedExpression();
        }
    }

    /**
     * Parses a single expression corresponding to a reporter block.
     *
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks The JsonNode holding all blocks of the actor definition currently analysed.
     * @return The parsed expression.
     * @throws ParsingException If the block is not parsable.
     */
    public static Expression parseExprBlock(String blockId, JsonNode exprBlock, JsonNode allBlocks)
            throws ParsingException {
        if (exprBlock instanceof ArrayNode) {
            // it's a list or variable
            String idString = exprBlock.get(2).asText(); // TODO: 2 is identifier pos
            String idName = exprBlock.get(1).asText(); // TODO: 1 is identifier name
            BlockMetadata metadata = BlockMetadataParser.parse(blockId, exprBlock);

            String currentActorName = ActorDefinitionParser.getCurrentActor().getName();
            if (ProgramParser.symbolTable.getVariable(idString, idName, currentActorName).isPresent()) {
                VariableInfo variableInfo
                        = ProgramParser.symbolTable.getVariable(idString, idName, currentActorName).get();

                return new Qualified(new StrId(variableInfo.getActor()),
                        new Variable(new StrId(variableInfo.getVariableName()), metadata));
            } else if (ProgramParser.symbolTable.getList(idString, idName, currentActorName).isPresent()) {
                Optional<ExpressionListInfo> listOptional
                        = ProgramParser.symbolTable.getList(idString, idName, currentActorName);
                ExpressionListInfo variableInfo = listOptional.get();
                return new Qualified(new StrId(variableInfo.getActor()),
                        new ScratchList(new StrId(variableInfo.getVariableName()), metadata));
            }
            throw new ParsingException("Id neither of List nor Variable.");
        } else {
            // it's a normal reporter block
            String opcode = exprBlock.get(OPCODE_KEY).asText();
            if (NumExprOpcode.contains(opcode)) {
                return NumExprParser.parseBlockNumExpr(blockId, exprBlock, allBlocks);
            } else if (StringExprOpcode.contains(opcode)) {
                return StringExprParser.parseBlockStringExpr(blockId, exprBlock, allBlocks);
            } else if (BoolExprOpcode.contains(opcode)) {
                return BoolExprParser.parseBlockBoolExpr(blockId, exprBlock, allBlocks);
            } else {
                throw new ParsingException(opcode + " is an unexpected opcode for an expression");
            }
        }
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    static ArrayNode getDataArrayByName(JsonNode inputs, String inputKey) throws ParsingException {
        final JsonNode dataArray = getExprArray(inputs, inputKey).get(POS_DATA_ARRAY);
        if (!(dataArray instanceof NullNode)) {
            return (ArrayNode) dataArray;
        } else {
            throw new ParsingException("Cannot parse null node as ArrayNode");
        }
    }

    static ArrayNode getExprArray(JsonNode inputs, String inputKey) {
        return (ArrayNode) inputs.get(inputKey);
    }
}
