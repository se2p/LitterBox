package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

/**
 * @QuestionType Free Text
 * @Highlighted Script
 * @Context Single script
 */
public class PurposeOfScript extends AbstractQuestionFinder {

    @Override
    public void visit(Script node) {
        ASTNode topBlockCurrent;
        if (!(node.getEvent() instanceof Never)) {
            topBlockCurrent = node.getEvent();
        } else {
            topBlockCurrent = node.getStmtList().getStmts().get(0);
        }
        IssueBuilder builder = prepareIssueBuilder(topBlockCurrent).withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        addIssue(builder.withScript(node).withHint(hint));
    }

    @Override
    public String getName() {
        return "purpose_of_script";
    }
}
