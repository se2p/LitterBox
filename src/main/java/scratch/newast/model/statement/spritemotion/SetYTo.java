package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.expression.num.NumExpr;

public class SetYTo implements SpriteMotionStmt {
    private NumExpr num;

    public SetYTo(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}