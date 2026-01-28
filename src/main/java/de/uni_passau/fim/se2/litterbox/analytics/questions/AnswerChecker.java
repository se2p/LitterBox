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

import java.util.*;

/**
 * Checks student answers against expected answers for comprehension questions.
 */
public class AnswerChecker {

    private final LLMAnswerEvaluator llmEvaluator;
    private final double numericTolerance;

    /**
     * Creates an AnswerChecker without LLM evaluation.
     */
    public AnswerChecker() {
        this(null, 0.0);
    }

    /**
     * Creates an AnswerChecker with optional LLM evaluation.
     *
     * @param llmEvaluator     Optional LLM evaluator for free text questions
     * @param numericTolerance Tolerance for numeric answers (e.g., 0.01 for 1% tolerance)
     */
    public AnswerChecker(LLMAnswerEvaluator llmEvaluator, double numericTolerance) {
        this.llmEvaluator = llmEvaluator;
        this.numericTolerance = numericTolerance;
    }

    /**
     * Checks all answers against the questions.
     *
     * @param questions       The list of questions
     * @param studentAnswers  Map of question ID to student's answer
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
                results.add(AnswerResult.simple(
                        question.id(),
                        question.questionType(),
                        false,
                        null,
                        formatExpectedAnswers(question.correctAnswers())
                ));
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
            // No correct answer defined, can't check
            return AnswerResult.simple(question.id(), question.questionType(), false, studentAnswer, null);
        }

        // Normalize both student answer and correct answers for comparison
        String normalizedStudent = normalizeAnswer(studentAnswer);
        boolean correct = question.correctAnswers().stream()
                .map(this::normalizeAnswer)
                .anyMatch(expected -> expected.equals(normalizedStudent));

        return AnswerResult.simple(
                question.id(),
                question.questionType(),
                correct,
                studentAnswer,
                formatExpectedAnswers(question.correctAnswers())
        );
    }

    private AnswerResult checkNumeric(QuestionDTO question, String studentAnswer) {
        if (question.correctAnswers() == null || question.correctAnswers().isEmpty()) {
            return AnswerResult.simple(question.id(), question.questionType(), false, studentAnswer, null);
        }

        try {
            double studentValue = Double.parseDouble(studentAnswer.trim());
            double expectedValue = Double.parseDouble(question.correctAnswers().get(0).trim());

            boolean correct;
            if (numericTolerance > 0) {
                correct = Math.abs(studentValue - expectedValue) <= Math.abs(expectedValue * numericTolerance);
            } else {
                correct = studentValue == expectedValue;
            }

            return AnswerResult.simple(
                    question.id(),
                    question.questionType(),
                    correct,
                    studentAnswer,
                    question.correctAnswers().get(0)
            );
        } catch (NumberFormatException e) {
            // Can't parse as number
            return AnswerResult.simple(
                    question.id(),
                    question.questionType(),
                    false,
                    studentAnswer,
                    question.correctAnswers().get(0)
            );
        }
    }

    private AnswerResult checkYesNo(QuestionDTO question, String studentAnswer) {
        if (question.correctAnswers() == null || question.correctAnswers().isEmpty()) {
            return AnswerResult.simple(question.id(), question.questionType(), false, studentAnswer, null);
        }

        String normalized = studentAnswer.trim().toLowerCase();
        boolean studentSaysYes = normalized.equals("yes") || normalized.equals("true") || normalized.equals("y");
        boolean studentSaysNo = normalized.equals("no") || normalized.equals("false") || normalized.equals("n");

        String expectedAnswer = question.correctAnswers().get(0).toLowerCase();
        boolean expectedYes = expectedAnswer.contains("yes") || expectedAnswer.equals("true");

        boolean correct;
        if (studentSaysYes) {
            correct = expectedYes;
        } else if (studentSaysNo) {
            correct = !expectedYes;
        } else {
            correct = false;
        }

        return AnswerResult.simple(
                question.id(),
                question.questionType(),
                correct,
                studentAnswer,
                question.correctAnswers().get(0)
        );
    }

    private AnswerResult checkFreeText(QuestionDTO question, String studentAnswer) {
        // If we have an LLM evaluator, use it
        if (llmEvaluator != null) {
            LLMEvaluation evaluation = llmEvaluator.evaluate(question, studentAnswer);
            return AnswerResult.withLLM(question.id(), question.questionType(), studentAnswer, evaluation);
        }

        // Without LLM, we can only do simple string matching if there are expected answers
        if (question.correctAnswers() != null && !question.correctAnswers().isEmpty()) {
            String normalizedStudent = normalizeAnswer(studentAnswer);
            boolean correct = question.correctAnswers().stream()
                    .map(this::normalizeAnswer)
                    .anyMatch(expected -> normalizedStudent.contains(expected) || expected.contains(normalizedStudent));

            return new AnswerResult(
                    question.id(),
                    question.questionType(),
                    correct,
                    studentAnswer,
                    formatExpectedAnswers(question.correctAnswers()),
                    correct ? 1.0 : 0.0,
                    "Free text answer requires manual review.",
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
                "Free text answer requires manual review.",
                null
        );
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        return answer.trim()
                .toLowerCase()
                .replace("[sbi]", "")
                .replace("[/sbi]", "")
                .replace("[var]", "")
                .replace("[/var]", "")
                .replace("[scratchblocks]", "")
                .replace("[/scratchblocks]", "")
                .replaceAll("\\s+", " ");
    }

    private String formatExpectedAnswers(List<String> answers) {
        if (answers == null || answers.isEmpty()) return null;
        return String.join(" | ", answers);
    }
}
