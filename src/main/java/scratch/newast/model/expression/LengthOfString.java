package scratch.newast.model.expression;

public class LengthOfString extends NumExpr {
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