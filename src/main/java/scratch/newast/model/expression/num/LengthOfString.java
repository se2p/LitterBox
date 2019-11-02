package scratch.newast.model.expression.num;

import scratch.newast.model.expression.string.StringExpr;

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