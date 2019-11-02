package scratch.newast.model.statement.spritelook;

import scratch.newast.model.expression.num.NumExpr;

public class ChangeLayerBy implements SpriteLookStmt {
    private NumExpr num;

    public ChangeLayerBy(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}