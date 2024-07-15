package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * @QuestionType Strings
 * @NumAnswers Multiple
 * @Highlighted Script
 * @Context Single script
 */
public class VariableInScript extends AbstractQuestionFinder {

    @Override
    public void visit(Script node) {
        answers.clear();
        super.visit(node);

        if (!answers.isEmpty()) {
            IssueBuilder builder = prepareIssueBuilder(node.getEvent()).withSeverity(IssueSeverity.LOW);
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.ANSWER, answers.toString());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(Variable node) {
        answers.add(node.getName().getScratchBlocks());
    }

    @Override
    public String getName() {
        return "variable_in_script";
    }
}
