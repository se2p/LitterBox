package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.ExpressionStmt;
import scratch.newast.model.statement.Stmt;
import scratch.newast.opcodes.BoolExprOpcode;
import scratch.newast.opcodes.NumExprOpcode;
import scratch.newast.opcodes.StringExprOpcode;
import scratch.newast.parser.ExpressionParser;

public class ExpressionStmtParser {

    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String opcode = current.get(Constants.OPCODE_KEY).asText();
        Stmt stmt;
        if (NumExprOpcode.contains(opcode)) {
            //NumExpr numExpr = ExpressionParser.parseNumExpr(current, allBlocks);
            NumExpr numExpr = null; //FIXME use the right arguments and then actually parse the expr
            stmt = new ExpressionStmt(numExpr);
        } else if (BoolExprOpcode.contains(opcode)) {
            BoolExpr boolExpr = ExpressionParser.parseBoolExpr(current, allBlocks);
            stmt = new ExpressionStmt(boolExpr);
        } else if (StringExprOpcode.contains(opcode)) {
            //StringExpr stringExpr = ExpressionParser.parseStringExpr(current, allBlocks);
            //stmt = new ExpressionStmt(stringExpr);
            stmt = new ExpressionStmt(null); //FIXME use the right arguments and then actually parse the expr
        } else {
            throw new ParsingException("There is no expression for opcode " + opcode);
        }

        return stmt;

    }
}
