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
 * Result of checking a single answer.
 *
 * @param questionId     The ID of the question
 * @param questionType   The type of question
 * @param correct        Whether the answer was correct
 * @param providedAnswer The answer provided by the student
 * @param expectedAnswer The expected correct answer(s)
 * @param partialCredit  Partial credit score (0.0 to 1.0)
 * @param feedback       Optional feedback message
 * @param llmEvaluation  LLM evaluation result for free text questions (null if not applicable)
 */
public record AnswerResult(
        int questionId,
        QuestionType questionType,
        boolean correct,
        String providedAnswer,
        String expectedAnswer,
        double partialCredit,
        String feedback,
        LLMEvaluation llmEvaluation
) {
    /**
     * Creates a simple correct/incorrect result without LLM evaluation.
     */
    public static AnswerResult simple(
            int questionId,
            QuestionType questionType,
            boolean correct,
            String providedAnswer,
            String expectedAnswer
    ) {
        return new AnswerResult(
                questionId,
                questionType,
                correct,
                providedAnswer,
                expectedAnswer,
                correct ? 1.0 : 0.0,
                null,
                null
        );
    }

    /**
     * Creates a result with feedback but no LLM evaluation.
     */
    public static AnswerResult withFeedback(
            int questionId,
            QuestionType questionType,
            boolean correct,
            String providedAnswer,
            String expectedAnswer,
            String feedback
    ) {
        return new AnswerResult(
                questionId,
                questionType,
                correct,
                providedAnswer,
                expectedAnswer,
                correct ? 1.0 : 0.0,
                feedback,
                null
        );
    }

    /**
     * Creates a result with LLM evaluation.
     */
    public static AnswerResult withLLM(
            int questionId,
            QuestionType questionType,
            String providedAnswer,
            LLMEvaluation evaluation
    ) {
        return new AnswerResult(
                questionId,
                questionType,
                evaluation.correct(),
                providedAnswer,
                null,
                evaluation.partialCredit(),
                evaluation.feedback(),
                evaluation
        );
    }
}
