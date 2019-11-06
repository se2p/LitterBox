package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.bool.BoolExpr;

public class StringContains implements BoolExpr {
    private final StringExpr first;
    private final StringExpr second;
    private final ImmutableList<ASTNode> children;

    public StringContains(StringExpr first, StringExpr second) {
        this.first = first;
        this.second = second;
        children = ImmutableList.<ASTNode>builder().add(first).add(second).build();
    }

    public StringExpr getFirst() {
        return first;
    }

    public StringExpr getSecond() {
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