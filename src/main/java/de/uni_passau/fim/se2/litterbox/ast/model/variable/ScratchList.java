package de.uni_passau.fim.se2.litterbox.ast.model.variable;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ScratchList extends DataExpr {

    public ScratchList(LocalIdentifier name) {
        super(name, new NoBlockMetadata());
    }

    public ScratchList(LocalIdentifier name, BlockMetadata metadata) {
        super(name, metadata);

    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
