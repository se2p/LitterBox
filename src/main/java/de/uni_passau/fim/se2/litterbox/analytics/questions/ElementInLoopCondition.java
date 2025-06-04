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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@code MAX_CHOICES}
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Script
 * @Context Single script
 */
public class ElementInLoopCondition extends AbstractQuestionFinder {

    private boolean insideCondition;

    @Override
    public void visit(Script node) {
        choices.clear();
        answers.clear();
        super.visit(node);
        choices.removeAll(answers);

        if (!answers.isEmpty() && !choices.isEmpty()) {

            ASTNode topBlockCurrent;
            if (!(node.getEvent() instanceof Never)) {
                topBlockCurrent = node.getEvent();
            } else {
                topBlockCurrent = node.getStmtList().getStmts().get(0);
            }
            IssueBuilder builder = prepareIssueBuilder(topBlockCurrent).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        insideCondition = true;
        NumExpr times = node.getTimes();
        if (times.hasChildren()) {
            visit(times);
        } else {
            visit((NumberLiteral) times);
        }
        insideCondition = false;
        visit(node.getStmtList());
    }

    @Override
    public void visit(UntilStmt node) {
        insideCondition = true;
        visit(node.getBoolExpr());
        insideCondition = false;
        visit(node.getStmtList());
    }

    @Override
    public void visit(Variable node) {
        if (insideCondition) {
            answers.add(wrappedScratchBlocks(node));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(NumExpr node) {
        if (node instanceof SingularExpression) {
            if (insideCondition) {
                answers.add(wrappedScratchBlocks(node));
            } else {
                choices.add(wrappedScratchBlocks(node));
            }
        } else {
            super.visit(node);
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideCondition) {
            answers.add(wrappedScratchBlocks(node));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(StringLiteral node) {
        if (insideCondition) {
            answers.add(wrappedScratchBlocks(node));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(ColorLiteral node) {
        if (insideCondition) {
            answers.add(wrappedScratchBlocks(node));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(ActorLookStmt node) {
        inLookStmt = true;
        super.visit(node);
        inLookStmt = false;
    }

    @Override
    public void visit(SpriteLookStmt node) {
        inLookStmt = true;
        super.visit(node);
        inLookStmt = false;
    }

    @Override
    public void visit(ActorSoundStmt node) {
        inSoundStmt = true;
        super.visit(node);
        inSoundStmt = false;
    }

    @Override
    public void visit(Broadcast node) {
        inBroadcastStmt = true;
        super.visit(node);
        inBroadcastStmt = false;
    }

    @Override
    public void visit(BroadcastAndWait node) {
        inBroadcastStmt = true;
        super.visit(node);
        inBroadcastStmt = false;
    }

    @Override
    public void visit(Qualified node) {
        // Don't add actor to lists
        if (node.getSecond() instanceof Variable variable) {
            visit(variable);
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        // Don't include keys as elements
    }

    @Override
    public void visit(KeyPressed node) {
        // Don't include keys as elements
    }

    @Override
    public String getName() {
        return "element_in_loop_condition";
    }
}
