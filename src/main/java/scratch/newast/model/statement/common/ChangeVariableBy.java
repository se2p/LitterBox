package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.variable.Variable;

public class ChangeVariableBy implements CommonStmt {
    private Variable variable;
    private Expression expr;
    private final ImmutableList<ASTNode> children;

    public ChangeVariableBy(Variable variable, Expression expr) {
        this.variable = variable;
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(variable).add(expr).build();
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

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}