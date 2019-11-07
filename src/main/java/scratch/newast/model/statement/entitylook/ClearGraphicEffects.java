package scratch.newast.model.statement.entitylook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ClearGraphicEffects implements EntityLookStmt {

    private final ImmutableList<ASTNode> children;

    public ClearGraphicEffects() {
        children = ImmutableList.<ASTNode>builder().build();
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