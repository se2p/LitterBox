package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context Single actor
 */
public class ScriptsForActor extends AbstractQuestionFinder {

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
        Hint hint = new Hint(getName());
        String actor = node.getIdent().getScratchBlocks();
        hint.setParameter(Hint.ACTOR, (actor.equals("Stage") ? IssueTranslator.getInstance().getInfo("stage") : actor));
        hint.setParameter(Hint.ANSWER, Integer.toString(node.getScripts().getSize()));
        addIssue(builder.withHint(hint));
    }

    @Override
    public String getName() {
        return "scripts_for_actor";
    }
}
