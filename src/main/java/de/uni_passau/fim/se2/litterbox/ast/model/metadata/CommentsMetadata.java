package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class CommentsMetadata extends AbstractNode implements Metadata {
    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
