package scratch.newast.model.statement;

import scratch.newast.model.expression.num.NumExpr;

public class SetSizeTo implements SpriteLookStmt {
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