package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;


import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class CloneOfMetadata extends AbstractNode implements BlockMetadata{
    private final BlockMetadata cloneBlockMetadata;
    private final BlockMetadata cloneMenuMetadata;

    public CloneOfMetadata(BlockMetadata cloneBlockMetadata, BlockMetadata cloneMenuMetadata) {
        super(cloneBlockMetadata,cloneMenuMetadata);
        this.cloneBlockMetadata = cloneBlockMetadata;
        this.cloneMenuMetadata = cloneMenuMetadata;
    }

    public BlockMetadata getCloneBlockMetadata() {
        return cloneBlockMetadata;
    }

    public BlockMetadata getCloneMenuMetadata() {
        return cloneMenuMetadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
