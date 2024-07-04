package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context One actor
 */
public class ScriptsForActor extends AbstractQuestionFinder {

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
        Hint hint = new Hint(getName());
        hint.setParameter(Hint.ACTOR, node.getIdent().getScratchBlocks());
        hint.setParameter(Hint.ANSWER, Integer.toString(node.getScripts().getSize()));
        addIssue(builder.withHint(hint));
    }

    @Override
    public String getName() {
        return "scripts_for_actor";
    }
}
