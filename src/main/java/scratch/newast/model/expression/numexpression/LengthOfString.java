package scratch.newast.model.expression.numexpression;

import scratch.newast.model.expression.StringExpr;
import scratch.newast.model.expression.numexpression.NumExpr;

public class LengthOfString implements NumExpr {
    private StringExpr stringExpr;

    public LengthOfString(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public void setStringExpr(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
    }
}