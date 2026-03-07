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

import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.NumberFormatUtil;
import de.uni_passau.fim.se2.litterbox.utils.ScratchBlocksFormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a question asking about the execution time of a wait statement.
 *
 */
public class WaitStatementExecution extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "wait_statement_execution";

    @Override
    public void visit(WaitSeconds node) {
        String seconds = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getSeconds()));

        List<String> distractors = generateDistractorsForValue(seconds);
        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(seconds), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withChoices(options)
                .withCorrectAnswers(Collections.singletonList(seconds));
        addQuestion(builder);
    }

    private List<String> generateDistractorsForValue(String value) {
        List<String> distractors = new ArrayList<>();
        try {
            double num = Double.parseDouble(value);
            distractors.add(NumberFormatUtil.format(num + 1));
            distractors.add(NumberFormatUtil.format(num - 1));
            distractors.add(NumberFormatUtil.format(num * 2));
            distractors.add("0");
            distractors.add(NumberFormatUtil.format(num / 2));
        } catch (NumberFormatException e) {
            distractors.add("1");
            distractors.add("0");
            distractors.add("10");
        }
        return distractors;
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
        return ScopeOfFocus.ATOM;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
