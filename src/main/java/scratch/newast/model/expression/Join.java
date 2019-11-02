package scratch.newast.model.expression;

public class Join implements StringExpr {
    private StringExpr first;
    private StringExpr second;

    public Join(StringExpr first, StringExpr second) {
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