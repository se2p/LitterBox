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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Loop
 * @Context Single script
 */
public class BlockControllingLoop extends AbstractQuestionFinder {

    private boolean loopFound;
    private IssueBuilder builder;

    public void visit(Script node) {
        loopFound = false;
        builder = null;
        choices.clear();
        answers.clear();
        super.visit(node);

        if (loopFound && choices.size() > 0) {
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    public void visit(RepeatTimesStmt node) {
        NumExpr times = node.getTimes();

        if (!loopFound) {
            loopFound = true;
            answers.add((times instanceof NumberLiteral numberLiteral) ?
                    wrappedScratchBlocks(numberLiteral) : wrappedScratchBlocks(times));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            visit(times);
        }
        visit(node.getStmtList());
    }

    public void visit(UntilStmt node) {
        if (!loopFound) {
            loopFound = true;
            answers.add(wrappedScratchBlocks(node.getBoolExpr()));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            choices.add(wrappedScratchBlocks(node.getBoolExpr()));
        }
        visit(node.getStmtList());
    }

    public void visit(ControlStmt node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof BoolExpr boolExpr) {
                visit(boolExpr);
            } else if (child instanceof Stmt stmt) {
                visit(stmt);
            } else {
                visit(child);
            }
        }
    }

    public void visit(Stmt node) {
        choices.add(wrappedScratchBlocks(node));
        super.visit(node);
    }

    public void visit(BoolExpr node) {
        choices.add(wrappedScratchBlocks(node));
        super.visit(node);
    }

    public void visit(NumExpr node) {
        if (node instanceof NumberLiteral numberLiteral) {
            choices.add(wrappedScratchBlocks(numberLiteral));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    public void visit(TerminationStmt node) {
        // Don't include Termination statements as options
    }

    @Override
    public String getName() {
        return "block_controlling_loop";
    }
}
