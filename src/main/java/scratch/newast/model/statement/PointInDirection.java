package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

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