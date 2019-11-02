package scratch.newast.model.statement.spritelook;

import scratch.newast.model.variable.Variable;

public class HideVariable implements SpriteLookStmt {
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