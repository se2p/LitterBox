package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.string.StringExpr;

public class ExpressionParser {

    public static Expression parseExpression(String expressionID,
        JsonNode blocks) { // TODO check if these params are sufficient/reasonable
        Expression expression = null;
        return expression;
    }

    public static NumExpr parseNumExpr(String numExprID,
        JsonNode numExprData) { // TODO check if these params are sufficient/reasonable
        NumExpr numExpr = null;
        return numExpr;
    }


    public static NumExpr parseNumExpr(JsonNode numExprData) { // TODO check if these params are sufficient/reasonable
        //This method is here in case we only have an "inputs" array, and not a real block
        NumExpr numExpr = null;
        return numExpr;
    }


    public static BoolExpr parseBoolExpr(String boolExprId,
        JsonNode boolExprData) { // TODO check if these params are sufficient/reasonable
        BoolExpr boolExpr = null;
        return boolExpr;
    }

    public static StringExpr parseStringExpr(String stringExprID,
        JsonNode stringExprData) { // TODO check if these params are sufficient/reasonable
        StringExpr stringExpr = null;
        return stringExpr;
    }

    public static StringExpr parseStringExpr(
        JsonNode stringExprData) { // TODO check if these params are sufficient/reasonable
        StringExpr stringExpr = null;
        return stringExpr;
    }


    public static Number parseNumber(String value) {
        Number number = null;
        return number;
    }
}