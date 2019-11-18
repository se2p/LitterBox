package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public interface ASTNode {

    void accept(ScratchVisitor visitor);

    ImmutableList<ASTNode> getChildren();

    default String getUniqueName() {
        return this.getClass().getSimpleName();
    }

}
