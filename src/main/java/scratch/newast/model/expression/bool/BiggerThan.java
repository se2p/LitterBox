package scratch.newast.model.expression.bool;

import scratch.newast.model.expression.num.NumExpr;

public class BiggerThan implements BoolExpr {
    private NumExpr first;
    private NumExpr second;

    public BiggerThan(NumExpr first, NumExpr second) {
        this.first = first;
        this.second = second;
    }

    public NumExpr getFirst() {
        return first;
    }

    public void setFirst(NumExpr first) {
        this.first = first;
    }

    public NumExpr getSecond() {
        return second;
    }

    public void setSecond(NumExpr second) {
        this.second = second;
    }
}