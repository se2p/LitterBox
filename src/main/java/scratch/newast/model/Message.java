package scratch.newast.model;

import com.google.common.collect.ImmutableList;

public class Message implements ASTNode {

    private final String message;
    private final ImmutableList<ASTNode> children;

    public Message(String message) {
        this.message = message;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getMessage() {
        return message;
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