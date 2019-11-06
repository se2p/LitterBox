package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.variable.Variable;

public class IndexOf implements NumExpr {
    private final Expression expr;
    private final Variable variable;
    private final ImmutableList<ASTNode> children;

    public IndexOf(Expression expr, Variable variable) {
        this.expr = expr;
        this.variable = variable;
        children = ImmutableList.<ASTNode>builder().add(expr).add(variable).build();
    }

    public Expression getExpr() {
        return expr;
    }

    public Variable getVariable() {
        return variable;
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