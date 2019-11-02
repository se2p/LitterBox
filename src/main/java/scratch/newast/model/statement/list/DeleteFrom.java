package scratch.newast.model.statement.list;

import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.statement.list.ListStmt;
import scratch.newast.model.variable.Variable;

public class DeleteFrom implements ListStmt {
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