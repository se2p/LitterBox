package scratch.newast.model.variable;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Concat implements Variable {
    private final Identifier first;
    private final Identifier second;
    private final ImmutableList<ASTNode> children;

    public Concat(Identifier first, Identifier second) {
        this.first = first;
        this.second = second;
        children =  ImmutableList.<ASTNode>builder().add(first).add(second).build();
    }

    public Identifier getFirst() {
        return first;
    }

    public Identifier getSecond() {
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