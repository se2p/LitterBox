package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

public class SetVolumeTo implements EntitySoundStmt {
    private NumExpr num;

    public SetVolumeTo(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}