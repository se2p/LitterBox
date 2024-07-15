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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;

import java.util.*;

import static java.util.Objects.isNull;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers Multiple
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Script
 * @Context Single script
 */
public class StatementTriggersEvent extends AbstractQuestionFinder {

    private Map<String, List<Script>> scriptsForEvent;
    private Map<String, Set<String>> eventStatements;
    private Map<Script, ActorDefinition> actorForScript;
    private boolean searchingForEvents;
    private boolean visitingStmts;
    private String foundEvent;

    @Override
    public void visit(Program node) {
        scriptsForEvent = new HashMap<>();
        eventStatements = new HashMap<>();
        actorForScript = new HashMap<>();

        searchingForEvents = true;
        super.visit(node);
        searchingForEvents = false;
        super.visit(node);

        scriptsForEvent.forEach((event, scripts) -> {
            if (!eventStatements.get(event).isEmpty()) {
                currentScript = scripts.get(0);
                currentActor = actorForScript.get(currentScript);

                IssueBuilder builder = prepareIssueBuilder(currentScript.getEvent()).withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                hint.setParameter(Hint.ANSWER, eventStatements.get(event).toString());
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(Script node) {
        if (searchingForEvents) {
            foundEvent = null;
            visit(node.getEvent());
            if (!isNull(foundEvent)) {
                scriptsForEvent.get(foundEvent).add(node);
                actorForScript.putIfAbsent(node, currentActor);
            }
        }
        else {
            visitingStmts = true;
            visit(node.getStmtList());
            visitingStmts = false;
        }
    }

    @Override
    public void visit(Event node) {
        if (searchingForEvents) {
            if (node instanceof ReceptionOfMessage)
                visit((ReceptionOfMessage) node);
            else if (node instanceof BackdropSwitchTo)
                visit((BackdropSwitchTo) node);
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (searchingForEvents) {
            foundEvent = node.getMsg().getScratchBlocks();
            scriptsForEvent.putIfAbsent(foundEvent, new ArrayList<>());
            eventStatements.putIfAbsent(foundEvent, new LinkedHashSet<>());
        }
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (searchingForEvents) {
            foundEvent = node.getBackdrop().getScratchBlocks();
            scriptsForEvent.putIfAbsent(foundEvent, new ArrayList<>());
            eventStatements.putIfAbsent(foundEvent, new LinkedHashSet<>());
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (!searchingForEvents) {
            String message = node.getMessage().getScratchBlocks();
            if (eventStatements.containsKey(message)) {
                eventStatements.get(message).add(wrappedScratchBlocks(node));
            } else {
                choices.add(wrappedScratchBlocks(node));
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (!searchingForEvents) {
            String message = node.getMessage().getScratchBlocks();
            if (eventStatements.containsKey(message)) {
                eventStatements.get(message).add(wrappedScratchBlocks(node));
            } else {
                choices.add(wrappedScratchBlocks(node));
            }
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (!searchingForEvents) {
            if (node.getElementChoice() instanceof WithExpr) {
                String backdrop = ((WithExpr) node.getElementChoice()).getExpression().getScratchBlocks();
                if (eventStatements.containsKey(backdrop)) {
                    eventStatements.get(backdrop).add(wrappedScratchBlocks(node));
                } else {
                    choices.add(wrappedScratchBlocks(node));
                }
            }
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (!searchingForEvents) {
            if (node.getElementChoice() instanceof WithExpr) {
                String backdrop = ((WithExpr) node.getElementChoice()).getExpression().getScratchBlocks();
                if (eventStatements.containsKey(backdrop)) {
                    eventStatements.get(backdrop).add(wrappedScratchBlocks(node));
                } else {
                    choices.add(wrappedScratchBlocks(node));
                }
            }
        }
    }

    @Override
    public void visit(NextBackdrop node) {
        // Ignore these and don't include as alternate choice
    }

    @Override
    public void visit(IfThenStmt node) {
        visit(node.getThenStmts());
    }

    public void visit(IfElseStmt node) {
        visit(node.getThenStmts());
        visit(node.getElseStmts());
    }

    @Override
    public void visit(LoopStmt node) {
        visit(node.getStmtList());
    }

    @Override
    public void visit(Stmt node) {
        if (visitingStmts)
            choices.add(wrappedScratchBlocks(node));
    }

    @Override
    public String getName() {
        return "statement_triggers_event";
    }
}
