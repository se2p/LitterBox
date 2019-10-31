package scratch.newast.model.statement;

import scratch.newast.model.variable.Variable;

public class ShowVariable extends SpriteLookStmt {
    private Variable variable;

    public ShowVariable(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}