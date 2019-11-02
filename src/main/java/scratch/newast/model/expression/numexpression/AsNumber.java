package scratch.newast.model.expression.numexpression;

import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.numexpression.NumExpr;

public class AsNumber implements NumExpr {
    private Expression expr;

    public AsNumber(Expression expr) {
        this.expr = expr;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
}