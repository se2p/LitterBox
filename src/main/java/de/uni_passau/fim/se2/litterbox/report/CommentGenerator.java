package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.CommentMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;

import java.io.IOException;
import java.util.Collection;

public class CommentGenerator implements ReportGenerator {

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {

        int numIssue = 0;
        for(Issue issue : issues) {
            ActorDefinition currentActor = issue.getActor();
            String hintText = issue.getHint();
            String commentId = issue.getFinderName() + numIssue++;
            Metadata metaData = issue.getCodeMetadata();
            if(metaData == null) {
                addLooseComment(currentActor, hintText, commentId);
            } else {
                addBlockComment((NonDataBlockMetadata) metaData,
                        currentActor,
                        hintText,
                        commentId);
            }
        }
    }

    private void addBlockComment(NonDataBlockMetadata metadata, ActorDefinition currentActor, String hintText,
                                       String commentId) {
        metadata.setCommentId(commentId);
        CommentMetadata comment = new CommentMetadata(commentId, metadata.getBlockId(), 500, 400, 100, 100, false,
                hintText);
        currentActor.getMetadata().addComment(comment);
    }

    private void addLooseComment(ActorDefinition currentActor, String hintText,
                                       String commentId) {
        CommentMetadata comment = new CommentMetadata(commentId, null, 500, 400, 100, 100, false,
                hintText);
        currentActor.getMetadata().addComment(comment);
    }
}
