package scratch.newast.model.statement;

import scratch.newast.model.variable.Variable;

public class HideVariable extends SpriteLookStmt {
    private Variable variable;

    public HideVariable(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}