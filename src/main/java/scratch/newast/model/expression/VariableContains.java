package scratch.newast.model.expression;

public class VariableContains implements BoolExpr {
    private StringExpr variable;
    private Expression expr;

    public VariableContains(StringExpr variable, Expression expr) {
        this.variable = variable;
        this.expr = expr;
    }

    public StringExpr getVariable() {
        return variable;
    }

    public void setVariable(StringExpr variable) {
        this.variable = variable;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
}