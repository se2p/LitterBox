package scratch.newast.parser.stmt;

import static scratch.newast.Constants.INPUTS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.expression.list.ExpressionListPlain;
import scratch.newast.model.statement.CallStmt;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.variable.Identifier;
import scratch.newast.parser.ExpressionParser;

public class CallStmtParser {
    public static Stmt parse(JsonNode current, String blockId, JsonNode blocks) throws ParsingException {
        List<Expression> expressions = new ArrayList<>();
        JsonNode inputNode = current.get(INPUTS_KEY);
        for (int i = 0; i < inputNode.size(); i++) {
            expressions.add(ExpressionParser.parseExpression(current,i,blocks));
        }

        return new CallStmt(new Identifier(blockId),new ExpressionList(new ExpressionListPlain(expressions)));
    }
}
