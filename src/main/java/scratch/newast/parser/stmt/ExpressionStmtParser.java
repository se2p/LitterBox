package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.ParsingException;
import scratch.newast.model.statement.ExpressionStmt;
import scratch.newast.model.statement.Stmt;
import scratch.newast.parser.ExpressionParser;

public class ExpressionStmtParser {

    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new ExpressionStmt(ExpressionParser.parseExpression(current, 0, allBlocks));
    }
}
