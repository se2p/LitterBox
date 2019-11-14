package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.list.*;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.opcodes.ListStmtOpcode;
import scratch.newast.parser.ExpressionParser;
import scratch.newast.parser.ProgramParser;
import scratch.newast.parser.symboltable.ExpressionListInfo;

import static scratch.newast.Constants.*;

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
        StringExpr expr = ExpressionParser.parseStringExpr(current, 0, allBlocks);

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
        NumExpr expr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
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
        StringExpr stringExpr = ExpressionParser.parseStringExpr(current, 0, allBlocks);
        NumExpr numExpr = ExpressionParser.parseNumExpr(current, 1, allBlocks);
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
        StringExpr stringExpr = ExpressionParser.parseStringExpr(current, 1, allBlocks);
        NumExpr numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);

        ExpressionListInfo info = getListInfo(current);
        if (info.isGlobal()) {
            return new ReplaceItem(stringExpr, numExpr, new Identifier(info.getVariableName()));
        } else {
            return new ReplaceItem(stringExpr, numExpr, new Qualified(new Identifier(info.getActor()),
                    new Identifier(info.getVariableName())));
        }
    }
}
