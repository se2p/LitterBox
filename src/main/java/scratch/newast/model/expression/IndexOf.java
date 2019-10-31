package scratch.newast.model.expression;

import scratch.newast.model.variable.Variable;

public class IndexOf extends NumExpr {
    private Expression expr;
    private Variable variable;

    public IndexOf(Expression expr, Variable variable) {
        this.expr = expr;
        this.variable = variable;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}