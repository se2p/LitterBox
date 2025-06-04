package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @QuestionType Free Text
 * @Highlighted Statement
 * @Context Whole program
 */
public class PurposeOfBroadcast extends AbstractQuestionFinder {

    private Set<String> broadcastMessages;

    @Override
    public void visit(Program node) {
        broadcastMessages = new LinkedHashSet<>();
        super.visit(node);
    }

    @Override
    public void visit(Broadcast node) {
        String message = ScratchBlocksVisitor.of(node.getMessage());
        if (!broadcastMessages.contains(message)) {
            IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            addIssue(builder.withHint(hint));

            broadcastMessages.add(message);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        String message = ScratchBlocksVisitor.of(node.getMessage());
        if (!broadcastMessages.contains(message)) {
            IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            addIssue(builder.withHint(hint));

            broadcastMessages.add(message);
        }
    }

    @Override
    public String getName() {
        return "purpose_of_broadcast";
    }
}
