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
package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.LIST_IDENTIFIER_POS;
import static scratch.newast.Constants.LIST_KEY;
import static scratch.newast.Constants.LIST_NAME_POS;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.list.AddTo;
import scratch.newast.model.statement.list.DeleteAllOf;
import scratch.newast.model.statement.list.DeleteOf;
import scratch.newast.model.statement.list.InsertAt;
import scratch.newast.model.statement.list.ListStmt;
import scratch.newast.model.statement.list.ReplaceItem;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.opcodes.ListStmtOpcode;
import scratch.newast.parser.NumExprParser;
import scratch.newast.parser.ProgramParser;
import scratch.newast.parser.StringExprParser;
import scratch.newast.parser.symboltable.ExpressionListInfo;

public class ListStmtParser {

    public static ListStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).textValue();
        Preconditions
            .checkArgument(ListStmtOpcode.contains(opcodeString), "Given blockID does not point to a list " +
                "statement block.");

        ListStmtOpcode opcode = ListStmtOpcode.valueOf(opcodeString);
        ListStmt stmt;

        switch (opcode) {
            case data_replaceitemoflist:
                stmt = parseReplaceItemOfList(current, allBlocks);
                return stmt;
            case data_insertatlist:
                stmt = parseInsertAtList(current, allBlocks);
                return stmt;
            case data_deletealloflist:
                stmt = parseDeleteAllOfList(current);
                return stmt;
            case data_deleteoflist:
                stmt = parseDeleteOfList(current, allBlocks);
                return stmt;
            case data_addtolist:
                stmt = parseAddToList(current, allBlocks);
                return stmt;
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static ListStmt parseAddToList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr expr = StringExprParser.parseStringExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new AddTo(expr, new Identifier(info.getVariableName()));
        } else {
            return new AddTo(expr, new Qualified(new Identifier(info.getActor()),
                new Identifier(info.getVariableName())));
        }
    }

    private static ExpressionListInfo getListInfo(JsonNode current) {
        JsonNode listNode = current.get(FIELDS_KEY).get(LIST_KEY);
        Preconditions.checkArgument(listNode.isArray());
        ArrayNode listArray = (ArrayNode) listNode;
        String identifier = listArray.get(LIST_IDENTIFIER_POS).textValue();
        ExpressionListInfo info = ProgramParser.symbolTable.getLists().get(identifier);
        Preconditions.checkArgument(info.getVariableName().equals(listArray.get(LIST_NAME_POS).textValue()));
        return info;
    }

    private static ListStmt parseDeleteOfList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        NumExpr expr = NumExprParser.parseNumExpr(current, 0, allBlocks);
        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new DeleteOf(expr, new Identifier(info.getVariableName()));
        } else {
            return new DeleteOf(expr, new Qualified(new Identifier(info.getActor()),
                new Identifier(info.getVariableName())));
        }
    }

    private static ListStmt parseDeleteAllOfList(JsonNode current) {
        Preconditions.checkNotNull(current);

        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new DeleteAllOf(new Identifier(info.getVariableName()));
        } else {
            return new DeleteAllOf(new Qualified(new Identifier(info.getActor()),
                new Identifier(info.getVariableName())));
        }
    }

    private static ListStmt parseInsertAtList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new InsertAt(stringExpr, numExpr, new Identifier(info.getVariableName()));
        } else {
            return new InsertAt(stringExpr, numExpr, new Qualified(new Identifier(info.getActor()),
                new Identifier(info.getVariableName())));
        }
    }

    private static ListStmt parseReplaceItemOfList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, 1, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new ReplaceItem(stringExpr, numExpr, new Identifier(info.getVariableName()));
        } else {
            return new ReplaceItem(stringExpr, numExpr, new Qualified(new Identifier(info.getActor()),
                new Identifier(info.getVariableName())));
        }
    }
}
