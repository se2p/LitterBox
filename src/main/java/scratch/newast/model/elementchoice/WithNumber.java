package scratch.newast.model.elementchoice;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class WithNumber implements ElementChoice {

    private final NumExpr numExpr;
    private final ImmutableList<ASTNode> children;

    public WithNumber(NumExpr numExpr) {
        this.numExpr = numExpr;
        children = ImmutableList.<ASTNode>builder().add(numExpr).build();
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
