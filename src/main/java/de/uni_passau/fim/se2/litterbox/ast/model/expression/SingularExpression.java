package de.uni_passau.fim.se2.litterbox.ast.model.expression;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class SingularExpression extends AbstractNode {
    private final BlockMetadata metadata;

    public SingularExpression(BlockMetadata metadata) {
        super(metadata);
        this.metadata = metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
