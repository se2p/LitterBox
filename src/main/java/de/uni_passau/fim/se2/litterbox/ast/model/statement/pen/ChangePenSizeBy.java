package de.uni_passau.fim.se2.litterbox.ast.model.statement.pen;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ChangePenSizeBy extends AbstractNode implements PenStmt {
    private final NumExpr value;
    private final BlockMetadata metadata;

    public ChangePenSizeBy(NumExpr value, BlockMetadata metadata) {
        super(value, metadata);
        this.value = Preconditions.checkNotNull(value);
        this.metadata=metadata;
    }

    public BlockMetadata getMetadata() {
        return metadata;
    }

    public NumExpr getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}