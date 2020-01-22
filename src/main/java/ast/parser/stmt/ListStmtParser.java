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
package ast.parser.stmt;

import static ast.Constants.*;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ast.ParsingException;
import ast.model.expression.num.NumExpr;
import ast.model.expression.string.StringExpr;
import ast.model.statement.list.*;
import ast.model.variable.Qualified;
import ast.model.variable.StrId;
import ast.opcodes.ListStmtOpcode;
import ast.parser.NumExprParser;
import ast.parser.ProgramParser;
import ast.parser.StringExprParser;
import ast.parser.symboltable.ExpressionListInfo;
import utils.Preconditions;

public class ListStmtParser {

    public static ListStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(ListStmtOpcode.contains(opcodeString), "Given blockID does not point to a list " +
                "statement block.");

        final ListStmtOpcode opcode = ListStmtOpcode.valueOf(opcodeString);

        switch (opcode) {
            case data_replaceitemoflist:
                return parseReplaceItemOfList(current, allBlocks);

            case data_insertatlist:
                return parseInsertAtList(current, allBlocks);

            case data_deletealloflist:
                return parseDeleteAllOfList(current);

            case data_deleteoflist:
                return parseDeleteOfList(current, allBlocks);

            case data_addtolist:
                return parseAddToList(current, allBlocks);

            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static ListStmt parseAddToList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr expr = StringExprParser.parseStringExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);

        return new AddTo(expr, new Qualified(new StrId(info.getActor()),
            new StrId(info.getVariableName())));
    }

    private static ExpressionListInfo getListInfo(JsonNode current) {
        JsonNode listNode = current.get(FIELDS_KEY).get(LIST_KEY);
        Preconditions.checkArgument(listNode.isArray());
        ArrayNode listArray = (ArrayNode) listNode;
        String identifier = listArray.get(LIST_IDENTIFIER_POS).asText();
        ExpressionListInfo info = ProgramParser.symbolTable.getLists().get(identifier);
        Preconditions.checkArgument(info.getVariableName().equals(LIST_ABBREVIATION + listArray.get(LIST_NAME_POS).asText()));
        return info;
    }

    private static ListStmt parseDeleteOfList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        NumExpr expr = NumExprParser.parseNumExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        return new DeleteOf(expr, new Qualified(new StrId(info.getActor()),
            new StrId(info.getVariableName())));
    }

    private static ListStmt parseDeleteAllOfList(JsonNode current) {
        Preconditions.checkNotNull(current);

        ExpressionListInfo info = getListInfo(current);
        return new DeleteAllOf(new Qualified(new StrId(info.getActor()),
            new StrId(info.getVariableName())));
    }

    private static ListStmt parseInsertAtList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        return new InsertAt(stringExpr, numExpr, new Qualified(new StrId(info.getActor()),
            new StrId(info.getVariableName())));
    }

    private static ListStmt parseReplaceItemOfList(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        StringExpr stringExpr = StringExprParser.parseStringExpr(current, 1, allBlocks);
        NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        return new ReplaceItem(stringExpr, numExpr, new Qualified(new StrId(info.getActor()),
            new StrId(info.getVariableName())));
    }
}
