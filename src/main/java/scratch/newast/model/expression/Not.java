package scratch.newast.model.expression;

public class Not extends BoolExpr {
    private BoolExpr boolExpr;

    public Not(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public void setBoolExpr(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
    }
}