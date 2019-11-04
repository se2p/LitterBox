package scratch.newast.model.statement;

import scratch.newast.model.expression.Expression;

public class ExpressionStmt {

    Expression expression;

    public ExpressionStmt(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}
