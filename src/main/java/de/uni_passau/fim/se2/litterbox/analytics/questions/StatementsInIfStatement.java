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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;

/**
 * If stmts are contained in if stmts.
 *
 */
public class StatementsInIfStatement extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "statements_in_if_statement";

    private int inIfStmt;
    private boolean visitingScript;

    @Override
    public void visit(Script node) {
        choices.clear();
        answers.clear();

        visitingScript = true;
        inIfStmt = 0;
        super.visit(node);
        visitingScript = false;
        choices.removeAll(answers);

        if (!answers.isEmpty() && !choices.isEmpty()) {
            ASTNode topBlockCurrent;
            if (!(node.getEvent() instanceof Never)) {
                topBlockCurrent = node.getEvent();
            } else {
                topBlockCurrent = node.getStmtList().getStmts().getFirst();
            }
            QuestionBuilder builder = prepareQuestionBuilder(topBlockCurrent)
                    .withScript(node)
                    .withChoices(generateMultipleChoiceOptions(answers, choices))
                    .withCorrectAnswers(getAnswersList());
            addQuestion(builder);
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        inIfStmt++;
        visit(node.getThenStmts());
        inIfStmt--;
    }

    @Override
    public void visit(IfElseStmt node) {
        inIfStmt++;
        visit(node.getThenStmts());
        visit(node.getElseStmts());
        inIfStmt--;
    }

    @Override
    public void visit(LoopStmt node) {
        if (inIfStmt == 0) {
            visit(node.getStmtList());
        }
    }

    @Override
    public void visit(Stmt node) {
        if (inIfStmt > 0) {
            answers.add(wrappedScratchBlocks(node));
        } else if (visitingScript) {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public String getName() {
        return FINDER_NAME;
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.TEXT;
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
