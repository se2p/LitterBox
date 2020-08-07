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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ListStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ActorDefinitionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ListStmtParser {

    /**
     * Parses a ListStmt for a given block id.
     *
     * @param blockId   of the block to be parsed
     * @param current   JsonNode the contains the ListStmt
     * @param allBlocks of this program
     * @return the parsed ListStmt
     * @throws ParsingException if the block cannot be parsed into an ListStmt
     */
    public static ListStmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(ListStmtOpcode.contains(opcodeString), "Given blockID does not point to a list "
                        + "statement block.");

        final ListStmtOpcode opcode = ListStmtOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case data_replaceitemoflist:
                return parseReplaceItemOfList(current, allBlocks, metadata);

            case data_insertatlist:
                return parseInsertAtList(current, allBlocks, metadata);

            case data_deletealloflist:
                return parseDeleteAllOfList(current, metadata);

            case data_deleteoflist:
                return parseDeleteOfList(current, allBlocks, metadata);

            case data_addtolist:
                return parseAddToList(current, allBlocks, metadata);

            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static ListStmt parseAddToList(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr expr = StringExprParser.parseStringExpr(current, ITEM_KEY, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info == null) {
            return new AddTo(expr, new UnspecifiedId(), metadata);
        }
        return new AddTo(expr, new Qualified(new StrId(info.getActor()),
                new ScratchList(new StrId(info.getVariableName()))), metadata);
    }

    private static ExpressionListInfo getListInfo(JsonNode current) {
        JsonNode listNode = current.get(FIELDS_KEY).get(LIST_KEY);
        Preconditions.checkArgument(listNode.isArray());
        ArrayNode listArray = (ArrayNode) listNode;
        String identifier = listArray.get(LIST_IDENTIFIER_POS).asText();
        String idName = listArray.get(LIST_NAME_POS).asText();
        String currentActorName = ActorDefinitionParser.getCurrentActor().getName();
        if (ProgramParser.symbolTable.getList(identifier, idName, currentActorName).isEmpty()) {
            return null;
        }
        Optional<ExpressionListInfo> info = ProgramParser.symbolTable.getList(identifier, idName, currentActorName);

        Preconditions.checkArgument(info.isPresent());
        Preconditions.checkArgument(info.get().getVariableName().equals(listArray.get(LIST_NAME_POS).asText()));
        return info.get();
    }

    private static ListStmt parseDeleteOfList(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        NumExpr expr = NumExprParser.parseNumExpr(current, INDEX_KEY, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info == null) {
            return new DeleteOf(expr, new UnspecifiedId(), metadata);
        }
        return new DeleteOf(expr, new Qualified(new StrId(info.getActor()),
                new ScratchList(new StrId(info.getVariableName()))), metadata);
    }

    private static ListStmt parseDeleteAllOfList(JsonNode current, BlockMetadata metadata) {
        Preconditions.checkNotNull(current);

        ExpressionListInfo info = getListInfo(current);
        if (info == null) {
            return new DeleteAllOf(new UnspecifiedId(), metadata);
        }
        return new DeleteAllOf(new Qualified(new StrId(info.getActor()),
                new ScratchList(new StrId(info.getVariableName()))), metadata);
    }

    private static ListStmt parseInsertAtList(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, ITEM_KEY, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, INDEX_KEY, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info == null) {
            return new InsertAt(stringExpr, numExpr, new UnspecifiedId(), metadata);
        }
        return new InsertAt(stringExpr, numExpr, new Qualified(new StrId(info.getActor()),
                new ScratchList(new StrId(info.getVariableName()))), metadata);
    }

    private static ListStmt parseReplaceItemOfList(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, ITEM_KEY, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, INDEX_KEY, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info == null) {
            return new ReplaceItem(stringExpr, numExpr, new UnspecifiedId(), metadata);
        }
        return new ReplaceItem(stringExpr, numExpr, new Qualified(new StrId(info.getActor()),
                new ScratchList(new StrId(info.getVariableName()))), metadata);
    }
}
