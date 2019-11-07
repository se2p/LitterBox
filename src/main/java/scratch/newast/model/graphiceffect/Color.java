package scratch.newast.model.graphiceffect;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Color implements GraphicEffect {

    private final ImmutableList<ASTNode> children;

    public Color() {
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