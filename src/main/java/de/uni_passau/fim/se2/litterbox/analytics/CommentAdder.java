package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;

public abstract class CommentAdder {

    public static void addComment(NonDataBlockMetadata metadata, ActorDefinition currentActor, String hintText,
                                  String commentId) {
        (metadata).setCommentId(commentId);
        CommentMetadata comment = new CommentMetadata(commentId, metadata.getBlockId(), 500, 400, 100, 100, false,
                hintText);
        currentActor.getMetadata().addComment(comment);
    }
}
