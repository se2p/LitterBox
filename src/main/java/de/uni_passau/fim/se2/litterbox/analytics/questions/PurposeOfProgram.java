package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

/**
 * @QuestionType Free Text
 * @Highlighted Nothing
 * @Context Whole program
 */
public class PurposeOfProgram extends AbstractQuestionFinder {

    @Override
    public void visit(Program node) {
        super.visit(node);          // only needed so that an actor is set
        currentScript = null;
        currentProcedure = null;

        IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
        Hint hint = new Hint(getName());
        addIssue(builder.withHint(hint));
    }

    @Override
    public String getName() {
        return "purpose_of_program";
    }
}
