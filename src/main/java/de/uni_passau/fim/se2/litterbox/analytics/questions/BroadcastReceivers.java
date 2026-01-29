/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@link AbstractQuestionFinder#maxChoices}
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Script
 * @Context Whole program
 */
public class BroadcastReceivers extends AbstractQuestionFinder {

    private Map<String, Set<String>> receiversForMessage;
    private Set<String> allBroadcasts;
    private boolean collectingReceivers;

    @Override
    public void visit(Program node) {
        allBroadcasts = new LinkedHashSet<>();
        receiversForMessage = new LinkedHashMap<>();

        // First pass: collect all receivers
        collectingReceivers = true;
        super.visit(node);
        collectingReceivers = false;

        // Second pass: find broadcast blocks and generate questions
        super.visit(node);
    }

    @Override
    public void visit(ActorDefinition node) {
        if (collectingReceivers) {
            currentActor = node;
            visit(node.getScripts());
        } else {
            currentActor = node;
            super.visit(node);
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (collectingReceivers) {
            StringExpr msgExpr = node.getMsg().getMessage();
            if (msgExpr instanceof StringLiteral literal) {
                String msg = literal.getText();
                receiversForMessage.computeIfAbsent(msg, k -> new LinkedHashSet<>()).add(currentActor.getIdent().getName());
            }
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (!collectingReceivers) {
            StringExpr messageExpr = node.getMessage().getMessage();
            if (messageExpr instanceof StringLiteral literal) {
                handleBroadcast(node, literal.getText());
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (!collectingReceivers) {
            StringExpr messageExpr = node.getMessage().getMessage();
            if (messageExpr instanceof StringLiteral literal) {
                handleBroadcast(node, literal.getText());
            }
        }
    }

    private void handleBroadcast(ASTNode node, String message) {
        // Only generate one question per unique message to avoid duplicates
        if (allBroadcasts.contains(message)) {
            return;
        }
        allBroadcasts.add(message);

        Set<String> receivers = receiversForMessage.getOrDefault(message, Collections.emptySet());
        List<String> allActors = program.getActorDefinitionList().getDefinitions().stream()
                .map(a -> a.getIdent().getName())
                .collect(Collectors.toList());

        List<String> options = generateMultipleChoiceOptions(receivers, allActors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.MESSAGE, message)
                .withChoices(options)
                .withCorrectAnswers(new ArrayList<>(receivers));
        addQuestion(builder);
    }

    @Override
    public String getName() {
        return "broadcast_receivers";
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.TEXT;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.RELATION;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
