package scratch.newast.model.numfunct;

import scratch.newast.model.expression.NumExpr;

public class NumFunctOf implements NumExpr {
    private NumFunct funct;
    private NumExpr num;

    public NumFunctOf(NumFunct funct, NumExpr num) {
        this.funct = funct;
        this.num = num;
    }

    public NumFunct getFunct() {
        return funct;
    }

    public void setFunct(NumFunct funct) {
        this.funct = funct;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}