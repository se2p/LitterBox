package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;
import scratch.newast.model.expression.StringExpr;
import scratch.newast.model.variable.Variable;

public class InsertStringAt implements ListStmt {
    private StringExpr string;
    private NumExpr index;
    private Variable variable;

    public InsertStringAt(StringExpr string, NumExpr index, Variable variable) {
        this.string = string;
        this.index = index;
        this.variable = variable;
    }

    public StringExpr getString() {
        return string;
    }

    public void setString(StringExpr string) {
        this.string = string;
    }

    public NumExpr getIndex() {
        return index;
    }

    public void setIndex(NumExpr index) {
        this.index = index;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}