package scratch.newast.model.expression.string;

import scratch.newast.model.expression.num.NumExpr;

public class LetterOf implements StringExpr {
    private NumExpr num;
    private StringExpr stringExpr;

    public LetterOf(NumExpr num, StringExpr stringExpr) {
        this.num = num;
        this.stringExpr = stringExpr;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public void setStringExpr(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
    }
}