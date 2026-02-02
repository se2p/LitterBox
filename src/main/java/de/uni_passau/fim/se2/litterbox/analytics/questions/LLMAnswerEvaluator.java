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

import com.fasterxml.jackson.databind.json.JsonMapper;
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
    private final JsonMapper objectMapper;
    private final String language;

    /**
     * Creates an LLMAnswerEvaluator using the default LLM API from configuration.
     */
    public LLMAnswerEvaluator() {
        this(LlmApiProvider.get(), "en");
    }

    /**
     * Creates an LLMAnswerEvaluator with a specific LLM API and language.
     *
     * @param llmApi   the LLM API to use
     * @param language the language code for feedback (e.g., "en", "de", "es", "nl")
     */
    public LLMAnswerEvaluator(LlmApi llmApi, String language) {
        this.llmApi = llmApi;
        this.objectMapper = JsonMapper.builder().build();
        this.language = language != null ? language : "en";
    }

    /**
     * Creates an LLMAnswerEvaluator using the default LLM API with the specified language.
     *
     * @param language the language code for feedback (e.g., "en", "de", "es", "nl")
     */
    public LLMAnswerEvaluator(String language) {
        this(LlmApiProvider.get(), language);
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
        String languageName = getLanguageName(language);
        return """
                You are a friendly and encouraging teacher evaluating answers from children learning to
                code with Scratch.
                You will receive a question, code context, and an answer.
                Evaluate whether the answer demonstrates correct understanding.

                IMPORTANT: Write all feedback in %s, addressing the user directly using "You".

                Evaluation Guidelines:
                - BE LENIENT: The users are children. Minor terminology mistakes or spelling errors should be
                  ignored if the core concept is correct.
                - FOCUS ON UNDERSTANDING: If the answer shows they understood the code's behavior or purpose,
                  mark it as CORRECT.

                Always respond with a valid JSON object in this exact format:
                {
                  "correct": true or false,
                  "confidence": a number from 0.0 to 1.0,
                  "feedback": "your feedback message in %s",
                  "partialCredit": a number from 0.0 to 1.0
                }

                Feedback guidelines:
                - Tone: Warm, encouraging, and simple. Avoid complex technical jargon.
                - Address the user directly: "You correctly identified..." NOT "The student..."
                - For CORRECT answers: Acknowledge what they got right. Use positive reinforcement
                  (e.g., "Great job!", "Exactly!").
                - For INCORRECT answers: Gently explain what happened versus what they thought. Be constructive.
                - For PARTIAL answers: Acknowledge correct parts and explain what's missing or incorrect.
                - Keep feedback concise (1-3 sentences) but educational.
                - When referencing Scratch blocks, use [sbi]block text[/sbi] notation,
                  e.g., [sbi]move (10) steps[/sbi].
                - For variables, use [var]variable name[/var] notation, e.g., [var]score[/var]
                - ALWAYS write feedback in %s.

                Be generous with partial credit for answers that show partial understanding.
                """.formatted(languageName, languageName, languageName);
    }

    private String getLanguageName(String langCode) {
        return switch (langCode.toLowerCase()) {
            case "de" -> "German";
            case "es" -> "Spanish";
            case "nl" -> "Dutch";
            default -> "English";
        };
    }

    /**
     * Generates a hint for an unanswered question using the LLM.
     *
     * @param question The question to generate a hint for
     * @return A hint string
     */
    public String generateHint(QuestionDTO question) {
        String prompt = buildHintPrompt(question);

        try {
            return llmApi.query(getHintSystemPrompt(), prompt).getLast().text();
        } catch (Exception e) {
            log.warning("LLM hint generation failed: " + e.getMessage());
            return null;
        }
    }

    private String getHintSystemPrompt() {
        String languageName = getLanguageName(language);
        return """
                You are a friendly and encouraging teacher helping a child understand Scratch code.
                The student has NOT answered the question.
                Provide a helpful hint or explanation that guides them toward the answer WITHOUT giving it
                away directly.

                IMPORTANT: Write the hint in %s, addressing the user directly using "You".

                Guidelines:
                - Tone: Warm, simple, and encouraging.
                - Address the user directly: "You correctly identified..." NOT "The student..."
                - Keep the hint concise (1-2 sentences).
                - When referencing Scratch blocks, use [sbi]block text[/sbi] notation.
                - For variables, use [var]variable name[/var] notation.
                - ALWAYS write feedback in %s.
                """.formatted(languageName, languageName);
    }

    private String buildHintPrompt(QuestionDTO question) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Question: ").append(question.questionText()).append("\n\n");

        if (question.scratchBlocksCode() != null && !question.scratchBlocksCode().isEmpty()) {
            String code = cleanScratchBlocksNotation(question.scratchBlocksCode());
            prompt.append("Code Context:\n").append(code).append("\n\n");
        }

        prompt.append("Please provide a hint for this question.");

        return prompt.toString();
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
            default -> prompt.append("- Is the answer correct?\n");
        }

        prompt.append("\nProvide your evaluation as JSON.");

        return prompt.toString();
    }

    private LLMEvaluation parseResponse(String response) {
        try {
            // Try to extract JSON from the response
            String jsonStr = extractJson(response);
            return objectMapper.readValue(jsonStr, LLMEvaluation.class);
        } catch (Exception e) {
            log.warning("Failed to parse LLM response as JSON: " + e.getMessage());
            // Try to extract fields manually from malformed JSON
            LLMEvaluation manualParse = parseManually(response);
            if (manualParse != null) {
                return manualParse;
            }
            // Fall back to heuristic interpretation
            return interpretHeuristically(response);
        }
    }

    /**
     * Attempts to manually extract fields from malformed JSON where strings may not be quoted.
     */
    private LLMEvaluation parseManually(String response) {
        try {
            String jsonStr = extractJson(response);

            // Extract "correct" field
            Boolean correct = extractBooleanField(jsonStr, "correct");
            if (correct == null) {
                return null;
            }

            // Extract "confidence" field
            Double confidence = extractDoubleField(jsonStr, "confidence");
            if (confidence == null) {
                confidence = 0.5;
            }

            // Extract "partialCredit" field
            Double partialCredit = extractDoubleField(jsonStr, "partialCredit");
            if (partialCredit == null) {
                partialCredit = correct ? 1.0 : 0.0;
            }

            // Extract "feedback" field - this is the tricky one that may be unquoted
            String feedback = extractFeedbackField(jsonStr);
            if (feedback == null || feedback.isEmpty()) {
                return null;
            }

            return new LLMEvaluation(correct, confidence, feedback, partialCredit);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean extractBooleanField(String json, String fieldName) {
        // Match "fieldName": true or "fieldName": false
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*(true|false)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return null;
    }

    private Double extractDoubleField(String json, String fieldName) {
        // Match "fieldName": 0.5 or "fieldName": 1
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*([0-9]*\\.?[0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return null;
    }

    private String extractFeedbackField(String json) {
        // First try quoted string: "feedback": "..."
        java.util.regex.Pattern quotedPattern = java.util.regex.Pattern.compile(
                "\"feedback\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher quotedMatcher = quotedPattern.matcher(json);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1).replace("\\\"", "\"").replace("\\n", "\n");
        }

        // Try unquoted string: "feedback": ... until next field or end
        // Find where "feedback": starts
        int feedbackStart = json.indexOf("\"feedback\"");
        if (feedbackStart < 0) {
            return null;
        }

        int colonPos = json.indexOf(':', feedbackStart);
        if (colonPos < 0) {
            return null;
        }

        int valueStart = colonPos + 1;
        // Skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        // Find where the value ends (next field or end of object)
        int valueEnd = json.length() - 1;
        // Look for pattern like ,"fieldName" or just "fieldName" (LLM may omit comma) or }
        java.util.regex.Pattern endPattern = java.util.regex.Pattern.compile(
                ",?\\s*\"(partialCredit|correct|confidence)\"\\s*:|\\}");
        java.util.regex.Matcher endMatcher = endPattern.matcher(json.substring(valueStart));
        if (endMatcher.find()) {
            valueEnd = valueStart + endMatcher.start();
        }

        String feedback = json.substring(valueStart, valueEnd).trim();
        // Remove trailing comma if present
        if (feedback.endsWith(",")) {
            feedback = feedback.substring(0, feedback.length() - 1).trim();
        }
        // Remove surrounding quotes if present
        if (feedback.startsWith("\"") && feedback.endsWith("\"")) {
            feedback = feedback.substring(1, feedback.length() - 1);
        }

        return feedback.isEmpty() ? null : feedback;
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
        // Try to extract just the feedback content if it looks like malformed JSON
        String feedback = extractFeedbackField(response);
        if (feedback == null || feedback.isEmpty()) {
            // If no feedback field found, use the whole response but clean it up
            feedback = response.replaceAll("\\{[^}]*\\}", "").trim();
            if (feedback.isEmpty()) {
                feedback = "Evaluation completed. Manual review recommended.";
            }
        }

        String lower = response.toLowerCase();

        // Look for indicators of correctness
        boolean correct = (lower.contains("\"correct\": true") || lower.contains("\"correct\":true"))
                || (lower.contains("correct") && !lower.contains("incorrect")
                && !lower.contains("\"correct\": false") && !lower.contains("\"correct\":false"));

        boolean incorrect = lower.contains("\"correct\": false") || lower.contains("\"correct\":false")
                || lower.contains("incorrect")
                || lower.contains("wrong")
                || lower.contains("not quite");

        if (correct && !incorrect) {
            return LLMEvaluation.correct(0.5, feedback);
        } else if (incorrect) {
            return LLMEvaluation.incorrect(0.5, feedback);
        } else {
            // Uncertain - give partial credit
            return LLMEvaluation.partial(0.4, feedback, 0.5);
        }
    }

    private String cleanScratchBlocksNotation(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .trim();
    }
}
