package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.dragmode.DragMode;

public class SetDragMode implements SpriteMotionStmt {
    private final DragMode dragMode;
    private final ImmutableList<ASTNode> children;

    public SetDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
        children = ImmutableList.<ASTNode>builder().add(dragMode).build();
    }

    public DragMode getDragMode() {
        return dragMode;
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