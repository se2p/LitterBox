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

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@link AbstractQuestionFinder#maxChoices}
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Block
 * @Context Block
 */
public class RepeatUntilStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(StmtList node) {
        for (int i = 0; i < node.getNumberOfStatements() - 1; i++) {
            Stmt stmt = node.getStatement(i);
            if (stmt instanceof UntilStmt untilStmt) {
                Stmt nextStmt = node.getStatement(i + 1);
                generateQuestion(untilStmt, nextStmt);
            }
        }
        // Continue traversal
        visitChildren(node);
    }

    private void generateQuestion(UntilStmt untilStmt, Stmt nextStmt) {
        BoolExpr condition = untilStmt.getBoolExpr();
        String conditionText = wrappedScratchBlocks(condition);

        // Next statement description (e.g. "Move (10) steps")
        String nextStmtText = wrappedScratchBlocks(nextStmt);

        // Loop body description
        String loopBodyText = "The blocks inside the loop";
        if (untilStmt.getStmtList().hasStatements()) {
            loopBodyText = "The block '" + wrappedScratchBlocks(untilStmt.getStmtList().getStatement(0)) + "' inside the loop";
        }

        boolean conditionValue = de.uni_passau.fim.se2.litterbox.utils.Randomness.nextBoolean();
        String valueText = conditionValue ? "TRUE" : "FALSE";

        String correct;
        List<String> distractors = new ArrayList<>();

        if (conditionValue) {
            // If condition is TRUE, loop terminates
            correct = "The block '" + nextStmtText + "' after the loop executes";
            distractors.add(loopBodyText + " executes");
            distractors.add("The loop repeats");
        } else {
            // If condition is FALSE, loop executes
            correct = loopBodyText + " executes";
            distractors.add("The block '" + nextStmtText + "' after the loop executes");
            distractors.add("The script stops");
        }

        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(correct), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(untilStmt)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.CONDITION, conditionText)
                .withQuestionParameter(QuestionText.VALUE, valueText)
                .withCorrectAnswers(Collections.singletonList(correct))
                .withChoices(options);

        addQuestion(builder);
    }

    @Override
    public String getName() {
        return "repeat_until_statement_execution";
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.EXECUTION;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.BLOCK;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
