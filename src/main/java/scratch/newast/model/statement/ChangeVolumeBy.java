package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

public class ChangeVolumeBy implements EntitySoundStmt {
    private NumExpr num;

    public ChangeVolumeBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}