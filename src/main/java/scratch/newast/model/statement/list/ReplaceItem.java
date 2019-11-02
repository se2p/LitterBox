package scratch.newast.model.statement.list;

import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.variable.Variable;

public class ReplaceItem implements ListStmt {
    private NumExpr index;
    private Variable variable;
    private StringExpr string;

    public ReplaceItem(NumExpr index, Variable variable, StringExpr string) {
        this.index = index;
        this.variable = variable;
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

    public StringExpr getString() {
        return string;
    }

    public void setString(StringExpr string) {
        this.string = string;
    }
}