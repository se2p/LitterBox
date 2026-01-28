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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApi;
import de.uni_passau.fim.se2.litterbox.llm.api.LlmApiProvider;
import de.uni_passau.fim.se2.litterbox.report.QuestionDTO;

import java.util.logging.Logger;

/**
 * Uses an LLM to evaluate free text answers for comprehension questions.
 */
public class LLMAnswerEvaluator {

    private static final Logger log = Logger.getLogger(LLMAnswerEvaluator.class.getName());

    private final LlmApi llmApi;
    private final ObjectMapper objectMapper;

    /**
     * Creates an LLMAnswerEvaluator using the default LLM API from configuration.
     */
    public LLMAnswerEvaluator() {
        this(LlmApiProvider.get());
    }

    /**
     * Creates an LLMAnswerEvaluator with a specific LLM API.
     */
    public LLMAnswerEvaluator(LlmApi llmApi) {
        this.llmApi = llmApi;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Evaluates a student's answer to a free text question using the LLM.
     *
     * @param question      The question being answered
     * @param studentAnswer The student's answer
     * @return An LLMEvaluation with the result
     */
    public LLMEvaluation evaluate(QuestionDTO question, String studentAnswer) {
        String prompt = buildPrompt(question, studentAnswer);

        try {
            String response = llmApi.query(getSystemPrompt(), prompt).getLast().text();
            return parseResponse(response);
        } catch (Exception e) {
            log.warning("LLM evaluation failed: " + e.getMessage());
            return LLMEvaluation.partial(0.5, "Evaluation failed. Manual review required.", 0.5);
        }
    }

    private String getSystemPrompt() {
        return """
            You are an expert teacher evaluating student answers to program comprehension questions about Scratch code.
            You will receive a question, code context, and a student's answer.
            Evaluate whether the answer demonstrates correct understanding.

            Always respond with a valid JSON object in this exact format:
            {
              "correct": true or false,
              "confidence": a number from 0.0 to 1.0,
              "feedback": "explanation for the student",
              "partialCredit": a number from 0.0 to 1.0
            }

            Be generous with partial credit for answers that show partial understanding.
            Provide constructive feedback that helps the student learn.
            """;
    }

    private String buildPrompt(QuestionDTO question, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Question Type: ").append(question.programDimension().name())
                .append("/").append(question.scopeOfFocus().name()).append("\n\n");

        prompt.append("Question: ").append(question.questionText()).append("\n\n");

        if (question.scratchBlocksCode() != null && !question.scratchBlocksCode().isEmpty()) {
            String code = cleanScratchBlocksNotation(question.scratchBlocksCode());
            prompt.append("Code Context:\n").append(code).append("\n\n");
        }

        prompt.append("Student's Answer: ").append(studentAnswer).append("\n\n");

        // Add guidance based on question type
        prompt.append("Evaluation Criteria:\n");
        switch (question.programDimension()) {
            case PURPOSE -> prompt.append("- Does the answer correctly explain the purpose or intent of the code?\n");
            case TEXT -> prompt.append("- Is the description of the code structure accurate?\n");
            case EXECUTION -> prompt.append("- Is the predicted behavior or execution correct?\n");
        }

        prompt.append("\nProvide your evaluation as JSON.");

        return prompt.toString();
    }

    private LLMEvaluation parseResponse(String response) {
        try {
            // Try to extract JSON from the response
            String jsonStr = extractJson(response);
            JsonNode json = objectMapper.readTree(jsonStr);

            boolean correct = json.has("correct") && json.get("correct").asBoolean(false);
            double confidence = json.has("confidence") ? json.get("confidence").asDouble(0.5) : 0.5;
            String feedback = json.has("feedback") ? json.get("feedback").asText("") : "";
            double partialCredit = json.has("partialCredit") ? json.get("partialCredit").asDouble(correct ? 1.0 : 0.0) : (correct ? 1.0 : 0.0);

            return new LLMEvaluation(correct, confidence, feedback, partialCredit);
        } catch (Exception e) {
            log.warning("Failed to parse LLM response as JSON: " + e.getMessage());
            // Try to interpret the response heuristically
            return interpretHeuristically(response);
        }
    }

    private String extractJson(String response) {
        // Find JSON object in the response
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private LLMEvaluation interpretHeuristically(String response) {
        String lower = response.toLowerCase();

        // Look for indicators of correctness
        boolean correct = lower.contains("correct") && !lower.contains("incorrect")
                || lower.contains("right answer")
                || lower.contains("well done")
                || lower.contains("good understanding");

        boolean incorrect = lower.contains("incorrect")
                || lower.contains("wrong")
                || lower.contains("not quite")
                || lower.contains("missing");

        if (correct && !incorrect) {
            return LLMEvaluation.correct(0.6, response);
        } else if (incorrect && !correct) {
            return LLMEvaluation.incorrect(0.6, response);
        } else {
            // Uncertain - give partial credit
            return LLMEvaluation.partial(0.4, response, 0.5);
        }
    }

    private String cleanScratchBlocksNotation(String text) {
        if (text == null) return "";
        return text.replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .trim();
    }
}
