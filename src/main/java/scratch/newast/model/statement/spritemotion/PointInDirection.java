package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.expression.num.NumExpr;

public class PointInDirection implements SpriteMotionStmt {
    private NumExpr direction;

    public PointInDirection(NumExpr direction) {
        this.direction = direction;
    }

    public NumExpr getDirection() {
        return direction;
    }

    public void setDirection(NumExpr direction) {
        this.direction = direction;
    }
}