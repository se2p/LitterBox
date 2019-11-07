package scratch.newast.model.spriteattribs;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class CostumeNumber implements SpriteAttribs {

    private final ImmutableList<ASTNode> children;

    public CostumeNumber() {
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