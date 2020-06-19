package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetRotationStyle extends AbstractNode implements SpriteMotionStmt {
    private RotationStyle rotation;
    private final BlockMetadata metadata;

    public SetRotationStyle(RotationStyle rotation, BlockMetadata metadata) {
        super(rotation, metadata);
        this.rotation = rotation;
        this.metadata = metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    public RotationStyle getRotation() {
        return rotation;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
