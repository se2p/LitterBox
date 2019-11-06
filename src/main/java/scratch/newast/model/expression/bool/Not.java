package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Not implements BoolExpr {

    private final BoolExpr boolExpr;
    private final ImmutableList<ASTNode> children;

    public Not(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
        children = ImmutableList.<ASTNode>builder().add(boolExpr).build();
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
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