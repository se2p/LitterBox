package scratch.newast.model.expression;

public class PickRandom extends NumExpr {
    private NumExpr from;
    private NumExpr to;

    public PickRandom(NumExpr from, NumExpr to) {
        this.from = from;
        this.to = to;
    }

    public NumExpr getFrom() {
        return from;
    }

    public void setFrom(NumExpr from) {
        this.from = from;
    }

    public NumExpr getTo() {
        return to;
    }

    public void setTo(NumExpr to) {
        this.to = to;
    }
}