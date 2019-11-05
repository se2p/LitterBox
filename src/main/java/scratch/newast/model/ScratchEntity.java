package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public class ScratchEntity implements ASTNode {

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<Object> getChildren() {
        return ImmutableList.builder().build();
    }
}
