package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.expression.list.ExpressionListPlain;
import scratch.newast.model.statement.CallStmt;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.variable.Identifier;

import java.util.ArrayList;
import java.util.List;

import static scratch.newast.Constants.INPUTS_KEY;

public class CallStmtParser {
    public static Stmt parse(JsonNode current, String blockId, JsonNode blocks) {
        List<Expression> expressions = new ArrayList<>();
        JsonNode input = current.get(INPUTS_KEY);

        return new CallStmt(new Identifier(blockId),new ExpressionList(new ExpressionListPlain(expressions)));
    }
}
