package scratch.newast.model.expression.num;

import scratch.newast.model.expression.Expression;

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