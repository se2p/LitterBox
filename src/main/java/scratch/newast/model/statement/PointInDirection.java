package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class PointInDirection extends SpriteMotionStmt {
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