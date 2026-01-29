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
