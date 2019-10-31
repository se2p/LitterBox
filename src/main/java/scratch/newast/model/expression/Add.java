package scratch.newast.model.expression;

public class Add extends NumExpr {
    private NumExpr first;
    private NumExpr second;

    public Add(NumExpr first, NumExpr second) {
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