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

import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
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
        TranslatableText nextStmtTranslatableText = QuestionText.fromKey("the_block_X_after_the_loop")
                .withQuestionParameter("X", wrappedScratchBlocks(nextStmt));

        // Loop body description
        TranslatableText loopBody;
        if (untilStmt.getStmtList().hasStatements()) {
            loopBody = QuestionText.fromKey("block_inside_loop")
                    .withQuestionParameter("BLOCK", wrappedScratchBlocks(untilStmt.getStmtList().getStatement(0)));
        } else {
            loopBody = QuestionText.fromKey("blocks_inside_loop");
        }

        boolean conditionValue = de.uni_passau.fim.se2.litterbox.utils.Randomness.nextBoolean();
        String valueText = conditionValue ? QuestionText.TRUE_VALUE : QuestionText.FALSE_VALUE;

        TranslatableText correct;
        List<TranslatableText> distractors = new ArrayList<>();

        if (conditionValue) {
            // If condition is TRUE, loop terminates
            correct = QuestionText.fromKey("subject_executes").withQuestionParameter("SUBJECT", nextStmtTranslatableText);
            distractors.add(QuestionText.fromKey("subject_executes").withQuestionParameter("SUBJECT", loopBody));
            distractors.add(QuestionText.fromKey("subject_repeats").withQuestionParameter("SUBJECT", QuestionText.fromKey("the_loop")));
        } else {
            // If condition is FALSE, loop executes
            correct = QuestionText.fromKey("subject_executes").withQuestionParameter("SUBJECT", loopBody);
            distractors.add(QuestionText.fromKey("subject_executes").withQuestionParameter("SUBJECT", nextStmtTranslatableText));
            distractors.add(QuestionText.fromKey("script_stops"));
        }

        List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(Collections.singletonList(correct), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(untilStmt)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.CONDITION, conditionText)
                .withQuestionParameter(QuestionText.VALUE, QuestionText.fromKey(valueText))
                .withTranslatableCorrectAnswers(Collections.singletonList(correct))
                .withTranslatableChoices(options);

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
