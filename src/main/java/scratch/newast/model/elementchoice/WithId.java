package scratch.newast.model.elementchoice;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Identifier;

public class WithId implements ElementChoice {

    private final Identifier ident;
    private final ImmutableList<ASTNode> children;

    public WithId(Identifier ident) {
        this.ident = ident;
        children = ImmutableList.<ASTNode>builder().add(ident).build();
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
