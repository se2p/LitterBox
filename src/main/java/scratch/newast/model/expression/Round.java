package scratch.newast.model.expression;

public class Round extends NumExpr {
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