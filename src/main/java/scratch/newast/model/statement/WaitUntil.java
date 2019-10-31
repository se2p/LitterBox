package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class WaitUntil extends CommonStmt {
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