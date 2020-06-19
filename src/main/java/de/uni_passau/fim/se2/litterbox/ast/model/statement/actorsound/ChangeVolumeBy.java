package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ChangeVolumeBy extends AbstractNode implements ActorSoundStmt {
    private final NumExpr volumeValue;
    private final BlockMetadata metadata;

    public ChangeVolumeBy(NumExpr volumeValue, BlockMetadata metadata) {
        super(volumeValue, metadata);
        this.volumeValue = volumeValue;
        this.metadata = metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    public NumExpr getVolumeValue() {
        return volumeValue;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}