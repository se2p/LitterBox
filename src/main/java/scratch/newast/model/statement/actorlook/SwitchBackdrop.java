package scratch.newast.model.statement.actorlook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.backdrop.Backdrop;

public class SwitchBackdrop implements ActorLookStmt {
    private final Backdrop backdrop;
    private final ImmutableList<ASTNode> children;

    public SwitchBackdrop(Backdrop backdrop) {
        this.backdrop = backdrop;
        children = ImmutableList.<ASTNode>builder().add(backdrop).build();
    }

    public Backdrop getBackdrop() {
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