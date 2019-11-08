package scratch.newast.model.expression.list;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;

public class ExpressionListPlain implements ASTNode {

    private final ImmutableList<ASTNode> children;
    private List<Expression> expressions;

    public ExpressionListPlain(List<Expression> expressions) {
        this.expressions = expressions;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<Expression> getExpressions() {
        return expressions;
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
