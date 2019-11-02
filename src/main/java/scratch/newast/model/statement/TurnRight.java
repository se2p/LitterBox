package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class TurnRight implements SpriteMotionStmt {
    private NumExpr degrees;

    public TurnRight(NumExpr degrees) {
        this.degrees = degrees;
    }

    public NumExpr getDegrees() {
        return degrees;
    }

    public void setDegrees(NumExpr degrees) {
        this.degrees = degrees;
    }
}