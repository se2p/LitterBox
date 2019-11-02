package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.expression.num.NumExpr;

public class MoveSteps implements SpriteMotionStmt {
    private NumExpr steps;

    public MoveSteps(NumExpr steps) {
        this.steps = steps;
    }

    public NumExpr getSteps() {
        return steps;
    }

    public void setSteps(NumExpr steps) {
        this.steps = steps;
    }
}