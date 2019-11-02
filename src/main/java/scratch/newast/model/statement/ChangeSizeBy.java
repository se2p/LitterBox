package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;

public class ChangeSizeBy implements SpriteLookStmt {
    private NumExpr num;

    public ChangeSizeBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}