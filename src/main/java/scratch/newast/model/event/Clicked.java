package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTLeaf;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Clicked implements Event, ASTLeaf {
    private final ImmutableList<ASTNode> children;

    public Clicked() {
        children = ImmutableList.<ASTNode>builder().build();
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