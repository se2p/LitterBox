package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Or implements BoolExpr {

    private final BoolExpr first;
    private final BoolExpr second;
    private final ImmutableList<ASTNode> children;

    public Or(BoolExpr first, BoolExpr second) {
        this.first = first;
        this.second = second;
        children = ImmutableList.<ASTNode>builder().add(first).add(second).build();
    }

    public BoolExpr getFirst() {
        return first;
    }


    public BoolExpr getSecond() {
        return second;
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