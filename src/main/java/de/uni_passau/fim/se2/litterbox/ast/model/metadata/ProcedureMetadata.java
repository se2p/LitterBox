package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ProcedureMetadata extends AbstractNode implements Metadata {
    private BlockMetadata definition;
    private BlockMetadata prototype;

    public ProcedureMetadata(BlockMetadata definition, BlockMetadata prototype) {
        super(definition, prototype);
        this.definition = definition;
        this.prototype = prototype;
    }

    public BlockMetadata getDefinition() {
        return definition;
    }

    public BlockMetadata getPrototype() {
        return prototype;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
