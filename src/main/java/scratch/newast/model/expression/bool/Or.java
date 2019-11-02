package scratch.newast.model.expression.bool;

public class Or implements BoolExpr {
    private BoolExpr first;
    private BoolExpr second;

    public Or(BoolExpr first, BoolExpr second) {
        this.first = first;
        this.second = second;
    }

    public BoolExpr getFirst() {
        return first;
    }

    public void setFirst(BoolExpr first) {
        this.first = first;
    }

    public BoolExpr getSecond() {
        return second;
    }

    public void setSecond(BoolExpr second) {
        this.second = second;
    }
}