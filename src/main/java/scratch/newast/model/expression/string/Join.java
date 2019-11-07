package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Join implements StringExpr {

    private final StringExpr first;
    private final StringExpr second;
    private final ImmutableList<ASTNode> children;

    public Join(StringExpr first, StringExpr second) {
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
        return null;
    }
}