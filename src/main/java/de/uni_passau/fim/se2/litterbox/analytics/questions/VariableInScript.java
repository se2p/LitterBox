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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * Generates a question asking to identify a variable used in a specific script.
 *
 */
public class VariableInScript extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "variable_in_script";

    @Override
    public void visit(Script node) {
        answers.clear();
        super.visit(node);

        if (!answers.isEmpty()) {
            ASTNode topBlockCurrent;
            if (!(node.getEvent() instanceof Never)) {
                topBlockCurrent = node.getEvent();
            } else if (!node.getStmtList().getStmts().isEmpty()) {
                topBlockCurrent = node.getStmtList().getStmts().getFirst();
            } else {
                // Empty script with no hat block - nothing to highlight meaningfully or use as context anchor
                return;
            }
            QuestionBuilder builder = prepareQuestionBuilder(topBlockCurrent)
                    .withScript(node)
                    .withCorrectAnswers(getAnswersList());
            addQuestion(builder);
        }
    }

    @Override
    public void visit(Variable node) {
        answers.add(node.getName().getName());
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
        return QuestionType.FREE_TEXT;
    }
}
