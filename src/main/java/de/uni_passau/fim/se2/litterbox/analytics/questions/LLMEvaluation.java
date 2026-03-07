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

/**
 * Result of LLM-based evaluation for free text answers.
 *
 * @param correct       Whether the answer is considered correct
 * @param confidence    Confidence level of the evaluation (0.0 to 1.0)
 * @param feedback      Feedback for the student
 * @param partialCredit Partial credit score (0.0 to 1.0)
 */
public record LLMEvaluation(
        boolean correct,
        double confidence,
        String feedback,
        double partialCredit
) {
    /**
     * Creates an evaluation indicating the answer is correct.
     */
    public static LLMEvaluation correct(double confidence, String feedback) {
        return new LLMEvaluation(true, confidence, feedback, 1.0);
    }

    /**
     * Creates an evaluation indicating the answer is incorrect.
     */
    public static LLMEvaluation incorrect(double confidence, String feedback) {
        return new LLMEvaluation(false, confidence, feedback, 0.0);
    }

    /**
     * Creates an evaluation with partial credit.
     */
    public static LLMEvaluation partial(double confidence, String feedback, double partialCredit) {
        return new LLMEvaluation(partialCredit >= 0.5, confidence, feedback, partialCredit);
    }
}
