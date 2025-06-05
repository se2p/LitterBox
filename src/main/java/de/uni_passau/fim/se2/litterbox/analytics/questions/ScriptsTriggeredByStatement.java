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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@link AbstractQuestionFinder#maxChoices}
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Statement
 * @Context Whole program
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
            if (scriptsForEvent.containsKey(event)
                    && !scriptsForEvent.get(event).isEmpty()) {
                answers = scriptsForEvent.get(event);
                scriptsForEvent.forEach((e, scripts) -> choices.addAll(scripts));
                choices.removeAll(answers);

                currentScript = scriptForStmt.get(stmt);
                currentActor = actorForStmt.get(stmt);

                IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.CHOICES, getChoices());
                hint.setParameter(Hint.ANSWER, getAnswers());
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(Event node) {
        String event;
        if (node instanceof ReceptionOfMessage receptionOfMessage) {
            event = ScratchBlocksVisitor.of(receptionOfMessage.getMsg());
        } else if (node instanceof BackdropSwitchTo backdropSwitchTo) {
            event = ScratchBlocksVisitor.of(backdropSwitchTo.getBackdrop());
        } else {
            event = ScratchBlocksVisitor.of(node);
        }
        if (!(node instanceof Never)) {
            scriptsForEvent.computeIfAbsent(event, k -> new HashSet<>()).add(wrappedScratchBlocks(currentScript));
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (!isNull(currentScript)) {
            String message = ScratchBlocksVisitor.of(node.getMessage());
            eventStatements.putIfAbsent(message, node);
            scriptForStmt.putIfAbsent(node, currentScript);
            actorForStmt.putIfAbsent(node, currentActor);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (!isNull(currentScript)) {
            String message = ScratchBlocksVisitor.of(node.getMessage());
            eventStatements.putIfAbsent(message, node);
            scriptForStmt.putIfAbsent(node, currentScript);
            actorForStmt.putIfAbsent(node, currentActor);
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (!isNull(currentScript) && node.getElementChoice() instanceof WithExpr) {
            String backdrop = ScratchBlocksVisitor.of(((WithExpr) node.getElementChoice()).getExpression());
            eventStatements.putIfAbsent(backdrop, node);
            scriptForStmt.putIfAbsent(node, currentScript);
            actorForStmt.putIfAbsent(node, currentActor);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (!isNull(currentScript) && node.getElementChoice() instanceof WithExpr) {
            String backdrop = ScratchBlocksVisitor.of(((WithExpr) node.getElementChoice()).getExpression());
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
