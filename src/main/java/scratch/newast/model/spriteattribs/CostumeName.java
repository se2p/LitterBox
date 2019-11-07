package scratch.newast.model.spriteattribs;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class CostumeName implements SpriteAttribs {

    private final ImmutableList<ASTNode> children;

    public CostumeName() {
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