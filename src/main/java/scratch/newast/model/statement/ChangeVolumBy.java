package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class ChangeVolumBy extends EntitySoundStmt {
    private NumExpr num;

    public ChangeVolumBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}