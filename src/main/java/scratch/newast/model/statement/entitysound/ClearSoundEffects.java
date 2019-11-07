package scratch.newast.model.statement.entitysound;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ClearSoundEffects implements EntitySoundStmt {

    private final ImmutableList<ASTNode> children;

    public ClearSoundEffects() {
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