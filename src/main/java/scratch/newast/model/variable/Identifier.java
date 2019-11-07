package scratch.newast.model.variable;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Identifier implements Variable {

    private final String value; // TODO check if this is correct
    private final ImmutableList<ASTNode> children;

    public Identifier(String value) {
        this.value = value;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getValue() {
        return value;
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