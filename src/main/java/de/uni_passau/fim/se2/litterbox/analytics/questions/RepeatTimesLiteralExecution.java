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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

/**
 * Generates questions asking how many times a statement inside a repeat loop will execute.
 * Only applies to repeat loops with literal (numeric) iteration counts.
 *
 * <p>Example question: "How many times will [move 10 steps] be executed?"
 */
public class RepeatTimesLiteralExecution extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "repeat_times_literal_execution";

    private boolean hasStop;
    private int inLoop;

    @Override
    protected void resetState() {
        super.resetState();
        this.hasStop = false;
        this.inLoop = 0;
    }

    @Override
    public void visit(LoopStmt node) {
        inLoop++;
        super.visit(node);
        inLoop--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (inLoop == 0) {
            inLoop++;
            hasStop = false;

            if (node.getTimes() instanceof NumberLiteral num) {
                super.visit(node);

                if (node.getStmtList().hasStatements() && !hasStop) {
                    ASTNode stmt = getSingleStmt(node.getStmtList().getStmts().getFirst());

                    QuestionBuilder builder = prepareQuestionBuilder(stmt)
                            .withQuestionText(QuestionText.fromKey(getName()))
                            .withQuestionParameter(QuestionText.STATEMENT, ScratchBlocksVisitor.of(stmt))
                            .withCorrectAnswers(String.valueOf(num.getValue()));
                    addQuestion(builder);
                }
            }
            inLoop--;
        } else {
            inLoop++;
            visit(node.getStmtList());
            inLoop--;
        }
    }

    @Override
    public void visit(TerminationStmt node) {
        hasStop = true;
    }

    @Override
    public String getName() {
        return FINDER_NAME;
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
        return QuestionType.NUMBER;
    }
}
