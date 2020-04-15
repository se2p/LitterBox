package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ActorMetadata extends AbstractNode implements Metadata {
    private List<CommentMetadata> commentsMetadata;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
