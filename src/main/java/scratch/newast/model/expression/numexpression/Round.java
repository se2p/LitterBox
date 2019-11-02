package scratch.newast.model.expression.numexpression;

import scratch.newast.model.expression.numexpression.NumExpr;

public class Round implements NumExpr {
    private NumExpr num;

    public Round(NumExpr num) {
        this.num = num;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}