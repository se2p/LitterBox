package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public enum EntityType implements ASTNode {
    stage,
    group,
    sprite,
    module,
    agent,
    entity;

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
