package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.num.NumExpr;

public class AsString implements StringExpr {

    private final Expression expression;
    private final ImmutableList<ASTNode> children;

    public AsString(Expression expression) {
        this.expression = expression;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(expression).build();
    }

    public Expression getExpression() {
        return expression;
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