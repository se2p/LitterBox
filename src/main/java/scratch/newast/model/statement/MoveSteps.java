package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class MoveSteps extends SpriteMotionStmt {
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