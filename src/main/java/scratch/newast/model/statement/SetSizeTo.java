package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class SetSizeTo extends SpriteLookStmt {
    private NumExpr percent;

    public SetSizeTo(NumExpr percent) {
        this.percent = percent;
    }

    public NumExpr getPercent() {
        return percent;
    }

    public void setPercent(NumExpr percent) {
        this.percent = percent;
    }
}