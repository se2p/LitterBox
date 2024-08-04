package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * @QuestionType Free Text
 * @Highlighted Procedure
 * @Context Single procedure
 */
public class PurposeOfProcedure extends AbstractQuestionFinder {

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        IssueBuilder builder = prepareIssueBuilder(node).withMetadata(node.getMetadata().getDefinition()).withSeverity(IssueSeverity.LOW);
        builder.withProcedure(node);
        Hint hint = new Hint(getName());
        addIssue(builder.withHint(hint));
    }

    @Override
    public String getName() {
        return "purpose_of_procedure";
    }
}
