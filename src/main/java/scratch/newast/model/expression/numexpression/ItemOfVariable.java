package scratch.newast.model.expression.numexpression;

import scratch.newast.model.expression.StringExpr;
import scratch.newast.model.expression.numexpression.NumExpr;
import scratch.newast.model.variable.Variable;

public class ItemOfVariable implements StringExpr {
    private NumExpr num;
    private Variable variable;

    public ItemOfVariable(NumExpr num, Variable variable) {
        this.num = num;
        this.variable = variable;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}