package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context Whole program
 */
public class ScriptsInProgram extends AbstractQuestionFinder {

    private int scriptsInProgram;

    @Override
    public void visit(ActorDefinitionList node) {
        scriptsInProgram = 0;
        super.visit(node);

        IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
        Hint hint = Hint.fromKey(getName());
        hint.setParameter(Hint.ANSWER, Integer.toString(scriptsInProgram));
        addIssue(builder.withHint(hint));
    }

    @Override
    public void visit(ActorDefinition node) {
        scriptsInProgram += node.getScripts().getSize();
        currentActor = node;
    }

    @Override
    public String getName() {
        return "scripts_in_program";
    }
}
