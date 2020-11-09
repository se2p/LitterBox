/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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
        for (Issue issue : issues) {
            ActorDefinition currentActor = issue.getActor();
            String hintText = issue.getHint();
            hintText = hintText.replace("\"", "\\\"");
            String commentId = issue.getFinderName() + numIssue++;
            Metadata metaData = issue.getCodeMetadata();
            if (metaData == null) {
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
        CommentMetadata comment = new CommentMetadata(commentId, metadata.getBlockId(), 500, 400, 300, 300, false,
                hintText);
        currentActor.getActorMetadata().addComment(comment);
    }

    private void addLooseComment(ActorDefinition currentActor, String hintText,
                                 String commentId) {
        CommentMetadata comment = new CommentMetadata(commentId, null, 500, 400, 300, 300, false,
                hintText);
        currentActor.getActorMetadata().addComment(comment);
    }
}
