package scratch.newast.model.statement.list;

import scratch.newast.model.variable.Variable;

public class DeleteAllOf implements ListStmt {
    private Variable variable;

    public DeleteAllOf(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}