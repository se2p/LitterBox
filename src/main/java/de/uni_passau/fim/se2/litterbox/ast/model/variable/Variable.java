package de.uni_passau.fim.se2.litterbox.ast.model.variable;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class Variable extends DataExpr {
    private final BlockMetadata metadata;

    public Variable(LocalIdentifier name) {
        super(name);
        this.metadata = new NoBlockMetadata();
    }

    public Variable(LocalIdentifier name, BlockMetadata metadata) {
        super(name);
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
