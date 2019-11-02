package scratch.newast.model.statement;

import scratch.newast.model.expression.Expression;
import scratch.newast.model.variable.Variable;

public class ChangeVariableBy implements CommonStmt {
    private Variable variable;
    private Expression expr;

    public ChangeVariableBy(Variable variable, Expression expr) {
        this.variable = variable;
        this.expr = expr;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
}