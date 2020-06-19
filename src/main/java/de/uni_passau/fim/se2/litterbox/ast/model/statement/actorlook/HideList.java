package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class HideList extends AbstractNode implements ActorLookStmt {

    private final Identifier identifier;
    private final BlockMetadata metadata;

    public HideList(Identifier identifier, BlockMetadata metadata) {
        super(identifier, metadata);
        this.identifier = Preconditions.checkNotNull(identifier);
        this.metadata = metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}

