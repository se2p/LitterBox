package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class PenWithParamMetadata extends AbstractNode implements BlockMetadata{
    private final BlockMetadata penBlockMetadata;
    private final BlockMetadata paramMetadata;

    public PenWithParamMetadata(BlockMetadata penBlockMetadata, BlockMetadata paramMetadata) {
        super(penBlockMetadata, paramMetadata);
        this.penBlockMetadata = penBlockMetadata;
        this.paramMetadata = paramMetadata;
    }

    public BlockMetadata getPenBlockMetadata() {
        return penBlockMetadata;
    }

    public BlockMetadata getParamMetadata() {
        return paramMetadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
