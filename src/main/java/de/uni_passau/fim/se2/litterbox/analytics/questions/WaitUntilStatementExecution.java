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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.Collections;

/**
 * @QuestionType YES/NO
 * @NumAnswers 1
 * @NumChoices 2
 * @Highlighted Block
 * @Context Block
 */
public class WaitUntilStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(StmtList node) {
        for (int i = 0; i < node.getNumberOfStatements() - 1; i++) {
            Stmt stmt = node.getStatement(i);
            if (stmt instanceof WaitUntil waitUntil) {
                Stmt nextStmt = node.getStatement(i + 1);
                generateQuestion(waitUntil, nextStmt);
            }
        }
        // Continue traversal
        visitChildren(node);
    }

    private void generateQuestion(WaitUntil waitUntil, Stmt nextStmt) {
        BoolExpr condition = waitUntil.getUntil();
        String conditionText = wrappedScratchBlocks((Expression) condition);

        // Next statement description
        String nextBlockText = wrappedScratchBlocks(getSingleStmt(nextStmt));
        boolean conditionValue = Randomness.nextBoolean();
        String valueText = conditionValue ? QuestionText.TRUE_VALUE : QuestionText.FALSE_VALUE;

        String correctAnswer = conditionValue ? QuestionText.YES_TERM : QuestionText.NO_TERM;
        String wrongAnswer = conditionValue ? QuestionText.NO_TERM : QuestionText.YES_TERM;

        QuestionBuilder builder = prepareQuestionBuilder(waitUntil)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.CONDITION, conditionText)
                .withQuestionParameter(QuestionText.VALUE, QuestionText.fromKey(valueText))
                .withQuestionParameter(QuestionText.NEXT_BLOCK, nextBlockText)
                .withQuestionType(QuestionType.YES_NO)
                .withCorrectAnswers(correctAnswer)
                .withChoices(generateMultipleChoiceOptions(
                        Collections.singletonList(correctAnswer),
                        Collections.singletonList(wrongAnswer)));
        addQuestion(builder);
    }

    @Override
    public String getName() {
        return "wait_until_statement_execution";
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
        return QuestionType.YES_NO; // or MULTIPLE_CHOICE with Yes/No choices
    }
}
