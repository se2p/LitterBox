package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class BlockMetadata extends AbstractNode implements Metadata {
    private CommentsMetadata commentsMetadata;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
