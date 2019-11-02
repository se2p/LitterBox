package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

public class WaitSeconds implements CommonStmt {
    private NumExpr seconds;

    public WaitSeconds(NumExpr seconds) {
        this.seconds = seconds;
    }

    public NumExpr getSeconds() {
        return seconds;
    }

    public void setSeconds(NumExpr seconds) {
        this.seconds = seconds;
    }
}