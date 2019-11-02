package scratch.newast.model.expression.string;

import scratch.newast.model.expression.bool.BoolExpr;

public class StringContains implements BoolExpr {
    private StringExpr first;
    private StringExpr second;

    public StringContains(StringExpr first, StringExpr second) {
        this.first = first;
        this.second = second;
    }

    public StringExpr getFirst() {
        return first;
    }

    public void setFirst(StringExpr first) {
        this.first = first;
    }

    public StringExpr getSecond() {
        return second;
    }

    public void setSecond(StringExpr second) {
        this.second = second;
    }
}