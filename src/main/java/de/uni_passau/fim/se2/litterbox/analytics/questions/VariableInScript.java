package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * @QuestionType Strings
 * @NumAnswers {@code MAX_CHOICES}
 * @Highlighted Script
 * @Context Single script
 */
public class VariableInScript extends AbstractQuestionFinder {

    @Override
    public void visit(Script node) {
        answers.clear();
        super.visit(node);

        if (!answers.isEmpty()) {
            ASTNode topBlockCurrent;
            if (!(node.getEvent() instanceof Never)) {
                topBlockCurrent = node.getEvent();
            } else {
                topBlockCurrent = node.getStmtList().getStmts().get(0);
            }
            IssueBuilder builder = prepareIssueBuilder(topBlockCurrent).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(Variable node) {
        answers.add(node.getName().getName());
    }

    @Override
    public String getName() {
        return "variable_in_script";
    }
}
