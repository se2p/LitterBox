package scratch.newast.model.event;

import scratch.newast.model.expression.NumExpr;
import scratch.newast.model.variable.Variable;

public class VariableAboveValue implements Event {
    private Variable variable;
    private NumExpr value;

    public VariableAboveValue(Variable variable, NumExpr value) {
        this.variable = variable;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public NumExpr getValue() {
        return value;
    }

    public void setValue(NumExpr value) {
        this.value = value;
    }
}