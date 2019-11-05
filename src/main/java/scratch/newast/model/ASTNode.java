package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public interface ASTNode {

    default void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    ImmutableList<ASTNode> getChildren();

}
