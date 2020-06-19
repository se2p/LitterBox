package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetDragMode extends AbstractNode implements SpriteMotionStmt {
    private DragMode drag;
    private final BlockMetadata metadata;

    public SetDragMode(DragMode drag, BlockMetadata metadata) {
        super(drag, metadata);
        this.drag = drag;
        this.metadata = metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    public DragMode getDrag() {
        return drag;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
