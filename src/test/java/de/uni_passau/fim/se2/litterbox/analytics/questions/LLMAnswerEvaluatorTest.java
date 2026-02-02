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

import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LLMAnswerEvaluatorTest {

    private LLMAnswerEvaluator evaluator;
    private LlmApi llmApi;

    @BeforeEach
    void setUp() {
        llmApi = Mockito.mock(LlmApi.class);
        evaluator = new LLMAnswerEvaluator(llmApi, "en");
    }

    @Test
    void testParseResponseCorrect() throws Exception {
        String json = """
                {
                  "correct": true,
                  "confidence": 0.9,
                  "feedback": "Great job!",
                  "partialCredit": 1.0
                }
                """;

        Method method = LLMAnswerEvaluator.class.getDeclaredMethod("parseResponse", String.class);
        method.setAccessible(true);
        LLMEvaluation result = (LLMEvaluation) method.invoke(evaluator, json);

        assertTrue(result.correct());
        assertEquals(0.9, result.confidence());
        assertEquals("Great job!", result.feedback());
        assertEquals(1.0, result.partialCredit());
    }

    @Test
    void testParseResponseWithMarkdown() throws Exception {
        String json = """
                Here is the evaluation:
                ```json
                {
                  "correct": false,
                  "confidence": 0.8,
                  "feedback": "Try again.",
                  "partialCredit": 0.2
                }
                ```
                I hope this helps!
                """;

        Method method = LLMAnswerEvaluator.class.getDeclaredMethod("parseResponse", String.class);
        method.setAccessible(true);
        LLMEvaluation result = (LLMEvaluation) method.invoke(evaluator, json);

        assertFalse(result.correct());
        assertEquals(0.8, result.confidence());
        assertEquals("Try again.", result.feedback());
        assertEquals(0.2, result.partialCredit());
    }

    @Test
    void testGetSystemPromptIndentation() throws Exception {
        Method method = LLMAnswerEvaluator.class.getDeclaredMethod("getSystemPrompt");
        method.setAccessible(true);
        String prompt = (String) method.invoke(evaluator);

        // Check if there are large gaps or weird indentations
        assertFalse(prompt.contains("                {"), "JSON should not be excessively indented");
        assertTrue(prompt.contains("{\n  \"correct\""), "JSON should be properly formatted");

        // Split by lines and check first non-empty line indentation
        String[] lines = prompt.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                // If the line starts with whitespace, it should be reasonable (e.g. 0-2 spaces if stripped by text block)
                // Text blocks strip common whitespace. So all lines should start with 0 spaces if they are aligned.
                assertFalse(line.startsWith("    "), "Lines should not have residual common indentation from text block: " + line);
            }
        }
    }
}
