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
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Checks student answers against expected answers for comprehension questions.
 */
public class AnswerChecker {

    private static final Logger log = Logger.getLogger(AnswerChecker.class.getName());

    private final LLMAnswerEvaluator llmEvaluator;
    private final double numericTolerance;
    private final IssueTranslator translator;

    /**
     * Creates an AnswerChecker without LLM evaluation.
     */
    public AnswerChecker() {
        this(null, 0.0, null);
    }

    /**
     * Creates an AnswerChecker with optional LLM evaluation.
     *
     * @param llmEvaluator     Optional LLM evaluator for free text questions
     * @param numericTolerance Tolerance for numeric answers (e.g., 0.01 for 1% tolerance)
     */
    public AnswerChecker(LLMAnswerEvaluator llmEvaluator, double numericTolerance) {
        this(llmEvaluator, numericTolerance, null);
    }

    /**
     * Creates an AnswerChecker with optional LLM evaluation and localization.
     *
     * @param llmEvaluator     Optional LLM evaluator for free text questions
     * @param numericTolerance Tolerance for numeric answers (e.g., 0.01 for 1% tolerance)
     * @param translator       Optional translator for localized feedback messages
     */
    public AnswerChecker(LLMAnswerEvaluator llmEvaluator, double numericTolerance, IssueTranslator translator) {
        this.llmEvaluator = llmEvaluator;
        this.numericTolerance = numericTolerance;
        this.translator = translator;
    }

    /**
     * Checks all answers against the questions.
     *
     * @param questions      The list of questions
     * @param studentAnswers Map of question ID to student's answer
     * @return Complete check result with block model breakdown
     */
    public AnswerCheckResult checkAnswers(
            List<QuestionDTO> questions,
            Map<Integer, String> studentAnswers
    ) {
        List<AnswerResult> results = new ArrayList<>();
        Map<Integer, ProgramDimension> questionDimensions = new HashMap<>();
        Map<Integer, ScopeOfFocus> questionScopes = new HashMap<>();

        for (QuestionDTO question : questions) {
            questionDimensions.put(question.id(), question.programDimension());
            questionScopes.put(question.id(), question.scopeOfFocus());

            String studentAnswer = studentAnswers.get(question.id());
            if (studentAnswer == null) {
                // No answer provided
                String feedback = null;
                if (llmEvaluator != null) {
                    feedback = llmEvaluator.generateHint(question);
                }

                if (feedback != null && !feedback.isEmpty()) {
                    results.add(AnswerResult.withFeedback(
                            question.id(),
                            question.questionType(),
                            false,
                            null,
                            formatExpectedAnswers(question.correctAnswers()),
                            feedback
                    ));
                } else {
                    results.add(AnswerResult.simple(
                            question.id(),
                            question.questionType(),
                            false,
                            null,
                            formatExpectedAnswers(question.correctAnswers())
                    ));
                }
                continue;
            }

            AnswerResult result = checkSingleAnswer(question, studentAnswer);
            results.add(result);
        }

        return AnswerCheckResult.fromResults(results, questionDimensions, questionScopes);
    }

    /**
     * Checks a single answer.
     */
    public AnswerResult checkSingleAnswer(QuestionDTO question, String studentAnswer) {
        return switch (question.questionType()) {
            case MULTIPLE_CHOICE -> checkMultipleChoice(question, studentAnswer);
            case NUMBER -> checkNumeric(question, studentAnswer);
            case YES_NO -> checkYesNo(question, studentAnswer);
            case FREE_TEXT -> checkFreeText(question, studentAnswer);
        };
    }

    private AnswerResult checkMultipleChoice(QuestionDTO question, String studentAnswer) {
        if (question.correctAnswers() == null || question.correctAnswers().isEmpty()) {
            return AnswerResult.withFeedback(question.id(), question.questionType(), false, studentAnswer, null,
                    getFeedback("evaluation_no_expected_answer"));
        }

        String normalizedStudent = normalizeAnswer(studentAnswer);
        boolean correct = question.correctAnswers().stream()
                .map(this::normalizeAnswer)
                .anyMatch(expected -> expected.equals(normalizedStudent));

        String feedback = correct
                ? getFeedback("evaluation_feedback_correct_choice")
                : getFeedback("evaluation_feedback_wrong_choice");

        return AnswerResult.withFeedback(
                question.id(),
                question.questionType(),
                correct,
                studentAnswer,
                formatExpectedAnswers(question.correctAnswers()),
                feedback
        );
    }

    private AnswerResult checkNumeric(QuestionDTO question, String studentAnswer) {
        if (question.correctAnswers() == null || question.correctAnswers().isEmpty()) {
            return AnswerResult.withFeedback(question.id(), question.questionType(), false, studentAnswer, null,
                    getFeedback("evaluation_no_expected_answer"));
        }

        try {
            double studentValue = Double.parseDouble(studentAnswer.trim());
            double expectedValue = Double.parseDouble(question.correctAnswers().getFirst().trim());

            boolean correct;
            if (numericTolerance > 0) {
                correct = Math.abs(studentValue - expectedValue) <= Math.abs(expectedValue * numericTolerance);
            } else {
                // Use Double.compare for exact comparison to avoid floating-point precision issues
                correct = Double.compare(studentValue, expectedValue) == 0;
            }

            String feedback = correct
                    ? getFeedback("evaluation_feedback_correct_number")
                    : getFeedback("evaluation_feedback_wrong_number");

            return AnswerResult.withFeedback(
                    question.id(),
                    question.questionType(),
                    correct,
                    studentAnswer,
                    question.correctAnswers().getFirst(),
                    feedback
            );
        } catch (NumberFormatException e) {
            return AnswerResult.withFeedback(
                    question.id(),
                    question.questionType(),
                    false,
                    studentAnswer,
                    question.correctAnswers().getFirst(),
                    getFeedback("evaluation_feedback_invalid_number")
            );
        }
    }

    private AnswerResult checkYesNo(QuestionDTO question, String studentAnswer) {
        if (question.correctAnswers() == null || question.correctAnswers().isEmpty()) {
            return AnswerResult.withFeedback(question.id(), question.questionType(), false, studentAnswer, null,
                    getFeedback("evaluation_no_expected_answer"));
        }

        String normalized = studentAnswer.trim().toLowerCase();
        boolean studentSaysYes = normalized.equals("yes") || normalized.equals("true") || normalized.equals("y");
        boolean studentSaysNo = normalized.equals("no") || normalized.equals("false") || normalized.equals("n");

        String expectedAnswer = question.correctAnswers().getFirst().toLowerCase();
        boolean expectedYes = expectedAnswer.contains("yes") || expectedAnswer.equals("true");

        boolean correct;
        if (studentSaysYes) {
            correct = expectedYes;
        } else if (studentSaysNo) {
            correct = !expectedYes;
        } else {
            correct = false;
        }

        String feedback = correct
                ? getFeedback("evaluation_feedback_correct_yesno")
                : getFeedback("evaluation_feedback_wrong_yesno");

        return AnswerResult.withFeedback(
                question.id(),
                question.questionType(),
                correct,
                studentAnswer,
                question.correctAnswers().getFirst(),
                feedback
        );
    }

    private AnswerResult checkFreeText(QuestionDTO question, String studentAnswer) {
        // First try simple string matching if there are expected answers
        // This handles cases like SetVariable where the answer is a simple value
        if (question.correctAnswers() != null && !question.correctAnswers().isEmpty()) {
            String normalizedStudent = normalizeAnswer(studentAnswer);
            List<String> normalizedExpected = question.correctAnswers().stream()
                    .map(this::normalizeAnswer)
                    .toList();
            boolean correct = normalizedExpected.stream()
                    .anyMatch(expected -> normalizedStudent.equals(expected)
                            || normalizedStudent.contains(expected)
                            || expected.contains(normalizedStudent));

            // If string matching found a match, return success
            if (correct) {
                return new AnswerResult(
                        question.id(),
                        question.questionType(),
                        true,
                        studentAnswer,
                        formatExpectedAnswers(question.correctAnswers()),
                        1.0,
                        getFeedback("evaluation_feedback_correct"),
                        null
                );
            }

            // Debug logging for troubleshooting
            log.fine("FREE_TEXT no exact match for question " + question.id() + " (" + question.finder() + "): "
                    + "student='" + normalizedStudent + "', expected=" + normalizedExpected);
        }

        // If no exact match and we have an LLM evaluator, use it for semantic evaluation
        if (llmEvaluator != null) {
            LLMEvaluation evaluation = llmEvaluator.evaluate(question, studentAnswer);
            return AnswerResult.withLLM(question.id(), question.questionType(), studentAnswer, evaluation);
        }

        // No match found and no LLM available - mark as incorrect with review note
        if (question.correctAnswers() != null && !question.correctAnswers().isEmpty()) {
            return new AnswerResult(
                    question.id(),
                    question.questionType(),
                    false,
                    studentAnswer,
                    formatExpectedAnswers(question.correctAnswers()),
                    0.0,
                    getFeedback("evaluation_feedback_freetext_review"),
                    null
            );
        }

        // No way to check free text without expected answers or LLM
        return new AnswerResult(
                question.id(),
                question.questionType(),
                false,
                studentAnswer,
                null,
                0.0,
                getFeedback("evaluation_feedback_freetext_review"),
                null
        );
    }

    private String getFeedback(String key) {
        if (translator != null) {
            String feedback = translator.getInfo(key);
            if (feedback != null && !feedback.equals(key)) {
                return feedback;
            }
        }
        return key;
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String normalized = answer
                .replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();

        // Strip outer parentheses used by scratchblocks for number/string values
        // e.g., "(0)" -> "0", "(hello)" -> "hello"
        if (normalized.startsWith("(") && normalized.endsWith(")")) {
            String inner = normalized.substring(1, normalized.length() - 1);
            // Only strip if it's a simple value (no nested parentheses that would be unbalanced)
            if (!inner.contains("(") && !inner.contains(")")) {
                normalized = inner;
            }
        }

        return normalized;
    }

    private String formatExpectedAnswers(List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        return String.join(" | ", answers);
    }
}
