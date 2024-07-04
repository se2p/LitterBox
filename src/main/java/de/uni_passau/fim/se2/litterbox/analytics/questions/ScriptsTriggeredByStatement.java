package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@code MAX_CHOICES}
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Statement
 * @Context Single script
 */
public class ScriptsTriggeredByStatement extends AbstractQuestionFinder {

    private Map<String, Set<String>> scriptsForEvent;
    private Map<String, Stmt> eventStatements;
    private Map<Stmt, Script> scriptForStmt;
    private Map<Stmt, ActorDefinition> actorForStmt;

    @Override
    public void visit(Program node) {
        scriptsForEvent = new HashMap<>();
        eventStatements = new HashMap<>();
        scriptForStmt = new HashMap<>();
        actorForStmt = new HashMap<>();

        super.visit(node);

        eventStatements.forEach((event, stmt) -> {
            if (scriptsForEvent.containsKey(event) &&
                    !scriptsForEvent.get(event).isEmpty()) {
                answers = scriptsForEvent.get(event);
                scriptsForEvent.forEach((e, scripts) -> choices.addAll(scripts));
                choices.removeAll(answers);

                currentScript = scriptForStmt.get(stmt);
                currentActor = actorForStmt.get(stmt);

                IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                hint.setParameter(Hint.ANSWER, getAnswers());
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(Event node) {
        String event;
        if (node instanceof ReceptionOfMessage receptionOfMessage)
            event = receptionOfMessage.getMsg().getScratchBlocks();
        else if (node instanceof BackdropSwitchTo backdropSwitchTo)
            event = backdropSwitchTo.getBackdrop().getScratchBlocks();
        else
            event = node.getScratchBlocks();

        scriptsForEvent.computeIfAbsent(event, k -> new HashSet<>()).add(wrappedScratchBlocks(currentScript));
    }

    @Override
    public void visit(Broadcast node) {
        String message = node.getMessage().getScratchBlocks();
        eventStatements.putIfAbsent(message, node);
        scriptForStmt.putIfAbsent(node, currentScript);
        actorForStmt.putIfAbsent(node, currentActor);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        String message = node.getMessage().getScratchBlocks();
        eventStatements.putIfAbsent(message, node);
        scriptForStmt.putIfAbsent(node, currentScript);
        actorForStmt.putIfAbsent(node, currentActor);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (node.getElementChoice() instanceof WithExpr) {
            String backdrop = ((WithExpr) node.getElementChoice()).getExpression().getScratchBlocks();
            eventStatements.putIfAbsent(backdrop, node);
            scriptForStmt.putIfAbsent(node, currentScript);
            actorForStmt.putIfAbsent(node, currentActor);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (node.getElementChoice() instanceof WithExpr) {
            String backdrop = ((WithExpr) node.getElementChoice()).getExpression().getScratchBlocks();
            eventStatements.putIfAbsent(backdrop, node);
            scriptForStmt.putIfAbsent(node, currentScript);
            actorForStmt.putIfAbsent(node, currentActor);
        }
    }

    @Override
    public String getName() {
        return "scripts_triggered_by_statement";
    }
}
