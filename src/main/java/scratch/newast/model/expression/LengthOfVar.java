package scratch.newast.model.expression;

import scratch.newast.model.variable.Variable;

public class LengthOfVar extends NumExpr {
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