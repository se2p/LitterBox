/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.*;

import static java.util.Objects.isNull;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@code MAX_CHOICES}
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

                ASTNode topBlockCurrent;
                if (!(currentScript.getEvent() instanceof Never)) {
                    topBlockCurrent = currentScript.getEvent();
                } else {
                    topBlockCurrent = currentScript.getStmtList().getStmts().get(0);
                }
                IssueBuilder builder = prepareIssueBuilder(topBlockCurrent).withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                answers = eventStatements.get(event);
                hint.setParameter(Hint.ANSWER, getAnswers());
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
        } else {
            visitingStmts = true;
            visit(node.getStmtList());
            visitingStmts = false;
        }
    }

    @Override
    public void visit(Event node) {
        if (searchingForEvents) {
            if (node instanceof ReceptionOfMessage) {
                visit((ReceptionOfMessage) node);
            } else if (node instanceof BackdropSwitchTo) {
                visit((BackdropSwitchTo) node);
            }
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (searchingForEvents) {
            foundEvent = ScratchBlocksVisitor.of(node.getMsg());
            scriptsForEvent.putIfAbsent(foundEvent, new ArrayList<>());
            eventStatements.putIfAbsent(foundEvent, new LinkedHashSet<>());
        }
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (searchingForEvents) {
            foundEvent = ScratchBlocksVisitor.of(node.getBackdrop());
            scriptsForEvent.putIfAbsent(foundEvent, new ArrayList<>());
            eventStatements.putIfAbsent(foundEvent, new LinkedHashSet<>());
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (!searchingForEvents) {
            String message = ScratchBlocksVisitor.of(node.getMessage());
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
            String message = ScratchBlocksVisitor.of(node.getMessage());
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
                String backdrop = ScratchBlocksVisitor.of(((WithExpr) node.getElementChoice()).getExpression());
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
                String backdrop = ScratchBlocksVisitor.of(((WithExpr) node.getElementChoice()).getExpression());
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
        if (visitingStmts) {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public String getName() {
        return "statement_triggers_event";
    }
}
