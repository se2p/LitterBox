package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public class Key implements ASTLeaf{

    private final String key; // Todo should this be a string?
    private final ImmutableList<ASTNode> children;

    public Key(String key) {
        this.key = key;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
    }

    public String getKey() {
        return key;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] returnArray = {key};
        return returnArray;
    }
}
