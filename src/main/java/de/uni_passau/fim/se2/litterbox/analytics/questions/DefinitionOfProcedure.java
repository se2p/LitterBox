package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DefinitionOfProcedure extends AbstractQuestionFinder {

    private Map<ProcedureDefinition, ActorDefinition> procedures;
    private Set<Script> scripts;

    @Override
    public void visit(ActorDefinitionList node) {
        procedures = new HashMap<>();
        scripts = new LinkedHashSet<>();

        super.visit(node);
        currentScript = null;
        currentProcedure = null;

        procedures.forEach((procedure, actor) -> {
            choices.clear();
            // prioritize other procedure definitions in list of choices
            procedures.forEach((p, a) -> {
                if (!p.equals(procedure)) {
                    currentActor = a;
                    choices.add(wrappedScratchBlocks(p));
                }
            });
            scripts.forEach(s -> {
                if (choices.size() < MAX_CHOICES)
                    choices.add(wrappedScratchBlocks(s));
            });

            currentActor = actor;
            String procedureName = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName()).get(procedure.getIdent()).getName();

            IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.PROCEDURE, procedureName);
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, wrappedScratchBlocks(procedure));
            addIssue(builder.withHint(hint));
        });
    }

    @Override
    public void visit(ProcedureDefinition node) {
        super.visit(node);
        procedures.put(node, currentActor);
    }

    @Override
    public void visit(Script node) {
        scripts.add(node);
    }

    @Override
    public String getName() {
        return "definition_of_procedure";
    }
}
