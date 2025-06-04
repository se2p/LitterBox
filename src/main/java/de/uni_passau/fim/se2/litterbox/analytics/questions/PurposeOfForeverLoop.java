package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

/**
 * @QuestionType Free Text
 * @Highlighted Loop
 * @Context Single script
 */
public class PurposeOfForeverLoop extends AbstractQuestionFinder {

    @Override
    public void visit(RepeatForeverStmt node) {
        IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        addIssue(builder.withHint(hint));

        super.visit(node);
    }

    @Override
    public String getName() {
        return "purpose_of_forever_loop";
    }
}
