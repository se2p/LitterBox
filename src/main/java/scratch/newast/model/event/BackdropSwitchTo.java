package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Identifier;

public class BackdropSwitchTo implements Event {
    private final Identifier backdrop;
    private final ImmutableList<ASTNode> children;

    public BackdropSwitchTo(Identifier backdrop) {
        this.backdrop = backdrop;
        children = ImmutableList.<ASTNode>builder().add(backdrop).build();
    }

    public Identifier getBackdrop() {
        return backdrop;
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