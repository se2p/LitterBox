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

import de.uni_passau.fim.se2.litterbox.report.QuestionDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AnswerCheckerHintTest {

    @Test
    void testUnansweredQuestionGetsHintWithLLM() {
        // Setup
        LLMAnswerEvaluator llmEvaluator = Mockito.mock(LLMAnswerEvaluator.class);
        when(llmEvaluator.generateHint(any(QuestionDTO.class))).thenReturn("This is a hint.");

        AnswerChecker checker = new AnswerChecker(llmEvaluator, 0.0);

        QuestionDTO question = new QuestionDTO(1, "finder", "name", QuestionType.FREE_TEXT,
                ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Question?", null,
                List.of("Expected"), null, null);

        // Execute
        // Pass empty map for student answers so the question is unanswered
        AnswerCheckResult result = checker.checkAnswers(List.of(question), Collections.emptyMap());

        // Verify
        assertEquals(1, result.results().size());
        AnswerResult answerResult = result.results().get(0);
        assertFalse(answerResult.correct());
        assertEquals("This is a hint.", answerResult.feedback());
    }

    @Test
    void testUnansweredQuestionNoHintWithoutLLM() {
        // Setup
        AnswerChecker checker = new AnswerChecker(null, 0.0);

        QuestionDTO question = new QuestionDTO(1, "finder", "name", QuestionType.FREE_TEXT,
                ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Question?", null,
                List.of("Expected"), null, null);

        // Execute
        AnswerCheckResult result = checker.checkAnswers(List.of(question), Collections.emptyMap());

        // Verify
        assertEquals(1, result.results().size());
        AnswerResult answerResult = result.results().get(0);
        assertFalse(answerResult.correct());
        assertNull(answerResult.feedback());
    }
}
