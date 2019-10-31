package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class WaitSeconds extends CommonStmt {
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