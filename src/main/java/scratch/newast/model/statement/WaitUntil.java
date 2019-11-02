package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

public class WaitUntil implements CommonStmt {
    private NumExpr until;

    public WaitUntil(NumExpr until) {
        this.until = until;
    }

    public NumExpr getUntil() {
        return until;
    }

    public void setUntil(NumExpr until) {
        this.until = until;
    }
}