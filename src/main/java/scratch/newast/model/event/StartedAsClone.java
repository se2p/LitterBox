package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTLeaf;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class StartedAsClone implements Event, ASTLeaf {

    private final ImmutableList<ASTNode> children;

    public StartedAsClone() {
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
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