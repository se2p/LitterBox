package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Div implements NumExpr {
    private final NumExpr first;
    private final NumExpr second;
    private final ImmutableList<ASTNode> children;

    public Div(NumExpr first, NumExpr second) {
        this.first = first;
        this.second = second;
        children = ImmutableList.<ASTNode>builder().add(first).add(second).build();
    }

    public NumExpr getFirst() {
        return first;
    }


    public NumExpr getSecond() {
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