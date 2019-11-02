package scratch.newast.model.expression.num;

import scratch.newast.model.variable.Variable;

public class LengthOfVar implements NumExpr {
    private Variable variable;

    public LengthOfVar(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}