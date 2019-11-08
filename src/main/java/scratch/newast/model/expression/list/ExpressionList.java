package scratch.newast.model.expression.list;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ExpressionList implements ASTNode {

    private final ImmutableList<ASTNode> children;
    private ExpressionListPlain expressionListPlain;

    public ExpressionList(ExpressionListPlain expressionListPlain) {
        this.expressionListPlain = expressionListPlain;
        children = ImmutableList.<ASTNode>builder().add(expressionListPlain).build();
    }

    public ExpressionListPlain getExpressionListPlain() {
        return expressionListPlain;
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
