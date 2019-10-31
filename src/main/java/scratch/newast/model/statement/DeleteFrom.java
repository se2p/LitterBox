package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;
import scratch.newast.model.variable.Variable;

public class DeleteFrom extends ListStmt {
    private NumExpr num;
    private Variable variable;

    public DeleteFrom(NumExpr num, Variable variable) {
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