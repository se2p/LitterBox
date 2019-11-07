package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class PickRandom implements NumExpr {
    private final NumExpr from;
    private final NumExpr to;
    private final ImmutableList<ASTNode> children;

    public PickRandom(NumExpr from, NumExpr to) {
        this.from = from;
        this.to = to;

        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(from).add(to).build();
    }

    public NumExpr getFrom() {
        return from;
    }

    public NumExpr getTo() {
        return to;
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