package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.*;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Nothing
 * @Context Whole program
 */
public class VariableForActor extends AbstractQuestionFinder {

    private Set<String> variables;
    private Set<String> actors;
    private Map<String, List<String>> variablesForActor;

    @Override
    public void visit(Program node) {
        actors = new LinkedHashSet<>();
        variables = new LinkedHashSet<>();
        variablesForActor = new HashMap<>();
        super.visit(node);

        variablesForActor.forEach((actor, variables) -> {
            choices.addAll(actors);
            choices.remove(actor);

            for (String variable : variables) {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_VARIABLE, variable);
                hint.setParameter(Hint.ANSWER, actor);
                hint.setParameter(Hint.CHOICES, getChoices());
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(ActorDefinition node) {
        String actor = node.isSprite() ? node.getIdent().getName() : "All sprites";
        actors.add(actor);

        variables.clear();
        currentActor = node;
        super.visit(node.getDecls()); // visit declaration statement list
        variablesForActor.put(actor, new ArrayList<>(variables));
    }

    @Override
    public void visit(Variable node) {
        variables.add(node.getName().getScratchBlocks());
    }

    @Override
    public String getName() {
        return "variable_for_actor";
    }
}
