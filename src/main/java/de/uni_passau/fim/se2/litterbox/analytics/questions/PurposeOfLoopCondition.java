package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * @QuestionType Free Text
 * @Highlighted Loop
 * @Context Single script
 */
public class PurposeOfLoopCondition extends AbstractQuestionFinder {

    @Override
    public void visit(UntilStmt node) {
        IssueBuilder builder = prepareIssueBuilder((node).getBoolExpr()).withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        addIssue(builder.withHint(hint));

        super.visit(node);
    }

    @Override
    public String getName() {
        return "purpose_of_loop_condition";
    }
}
