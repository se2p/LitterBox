package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public enum ActorType implements ASTNode {
    actor,
    stage,
    sprite;

    private final ImmutableList<ASTNode> children = ImmutableList.<ASTNode>builder().build();

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}
