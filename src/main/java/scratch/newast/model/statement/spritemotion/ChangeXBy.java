package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.expression.num.NumExpr;

public class ChangeXBy implements SpriteMotionStmt {
    private NumExpr num;

    public ChangeXBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}