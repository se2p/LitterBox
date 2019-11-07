package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;

public class AsNumber implements NumExpr {
    private final Expression expr;
    private final ImmutableList<ASTNode> children;

    public AsNumber(Expression expr) {
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(expr).build();
    }

    public Expression getExpr() {
        return expr;
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