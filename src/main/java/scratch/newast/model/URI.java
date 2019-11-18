package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public class URI implements ASTLeaf {

    private final String uri;
    private final ImmutableList<ASTNode> children;

    public URI(String uri) {
        this.uri = uri;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getUri() {
        return uri;
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
        String[] returnString = {uri};
        return returnString;
    }
}
