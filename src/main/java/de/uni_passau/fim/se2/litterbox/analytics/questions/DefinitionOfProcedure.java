package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Statement
 * @Context Single actor
 */
public class DefinitionOfProcedure extends AbstractQuestionFinder {

    private Map<String, ProcedureDefinition> procedures;
    private Map<String, CallStmt> procedureCalls;
    private Map<CallStmt, Script> scriptForCall;
    private Map<CallStmt, ActorDefinition> actorForCall;
    private Set<Script> scripts;

    @Override
    public void visit(ActorDefinition node) {
        procedures = new HashMap<>();
        procedureCalls = new HashMap<>();
        scriptForCall = new HashMap<>();
        actorForCall = new HashMap<>();
        scripts = new LinkedHashSet<>();

        super.visit(node);

        procedureCalls.forEach((ident, stmt) -> {
            if (procedures.containsKey(ident)) {
                choices.clear();
                // prioritize other procedure definitions in list of choices
                procedures.forEach((i, p) -> {
                    if (!i.equals(ident)) {
                        choices.add(wrappedScratchBlocks(p));
                    }
                });
                scripts.forEach(s -> {
                    if (choices.size() < MAX_CHOICES)
                        choices.add(wrappedScratchBlocks(s));
                });

                currentScript = scriptForCall.get(stmt);
                currentActor = actorForCall.get(stmt);

                IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                hint.setParameter(Hint.ANSWER, wrappedScratchBlocks(procedures.get(ident)));
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(CallStmt node) {
        procedureCalls.putIfAbsent(node.getIdent().getName(), node);
        scriptForCall.putIfAbsent(node, currentScript);
        actorForCall.putIfAbsent(node, currentActor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        String procedureName = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName()).get(node.getIdent()).getName();
        procedures.put(procedureName, node);
    }

    @Override
    public void visit(Script node) {
        super.visit(node);
        scripts.add(node);
    }

    @Override
    public String getName() {
        return "definition_of_procedure";
    }
}
