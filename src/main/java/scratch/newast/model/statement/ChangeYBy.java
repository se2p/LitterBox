package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class ChangeYBy implements SpriteMotionStmt {
    private NumExpr num;

    public ChangeYBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}