package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;

/**
 * @QuestionType Free Text
 * @Highlighted If Statement
 * @Context Single script
 */
public class PurposeOfIfCondition extends AbstractQuestionFinder {

    @Override
    public void visit(IfStmt node) {
        IssueBuilder builder = prepareIssueBuilder((node).getBoolExpr()).withSeverity(IssueSeverity.LOW);
        Hint hint = new Hint(getName());
        addIssue(builder.withHint(hint));

        super.visit(node);
    }

    @Override
    public String getName() {
        return "purpose_of_if_condition";
    }
}
