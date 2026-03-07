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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a question asking for a valid number within a random range (pick random X to Y).
 *
 */
public class RandomNumberRange extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "random_number_range";

    @Override
    public void visit(PickRandom node) {
        NumExpr fromExpr = node.getOperand1();
        NumExpr toExpr = node.getOperand2();

        if (fromExpr instanceof NumberLiteral from && toExpr instanceof NumberLiteral to) {
            double min = from.getValue();
            double max = to.getValue();

            // Scratch handles swapped min/max, but let's assume min <= max for simplicity
            // or just swap them here if needed for distractor logic.
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }

            boolean isInt = isInteger(min) && isInteger(max);

            // Generate question
            // Logic: Correct answer is a value in [min, max].
            // Distractors: min - 1, max + 1, decimal (if int), or way off.

            // We'll choose 'max' as the correct answer (since it tests the upper bound inclusiveness).
            // Or 'min'. Or a random value inside. Let's pick 'max' for consistency or maybe middle.
            // Let's use 'max' to test inclusive upper bound.
            String correct = formatNumber(max);

            List<String> distractors = new ArrayList<>();
            distractors.add(formatNumber(max + 1)); // Out of bounds high
            distractors.add(formatNumber(min - 1)); // Out of bounds low

            if (isInt) {
                // If integers, suggest a decimal (though scratch creates ints if inputs are ints)
                distractors.add(formatNumber(min + 0.5));
            } else {
                // If decimals, suggest something else
                distractors.add(formatNumber(max + 2.5));
            }

            choices.clear();
            choices.add(correct);
            choices.addAll(distractors);

            QuestionBuilder builder = prepareQuestionBuilder(node)
                    .withCorrectAnswers(Collections.singletonList(correct))
                    .withChoices(new ArrayList<>(choices));

            addQuestion(builder);
        }
    }

    private boolean isInteger(double d) {
        return d == Math.floor(d) && !Double.isInfinite(d);
    }

    private String formatNumber(double d) {
        if (isInteger(d)) {
            return String.valueOf((int) d);
        }
        return String.valueOf(d);
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
        return ScopeOfFocus.ATOM;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
