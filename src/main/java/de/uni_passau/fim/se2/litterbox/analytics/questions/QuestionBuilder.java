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

import de.uni_passau.fim.se2.litterbox.analytics.FindingBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for creating {@link Question} instances.
 * Extends {@link FindingBuilder} with question-specific fields.
 */
public class QuestionBuilder extends FindingBuilder<Question, QuestionBuilder> {

    private QuestionFinder finder;
    private QuestionType questionType;
    private ProgramDimension programDimension;
    private ScopeOfFocus scopeOfFocus;
    private String questionText;
    private List<String> choices;
    private List<String> correctAnswers;

    /**
     * Sets the finder that is creating this question.
     *
     * @param finder the question finder
     * @return this builder
     */
    public QuestionBuilder withFinder(QuestionFinder finder) {
        this.finder = finder;
        return self();
    }

    /**
     * Sets the question type.
     *
     * @param questionType the type of answer expected
     * @return this builder
     */
    public QuestionBuilder withQuestionType(QuestionType questionType) {
        this.questionType = questionType;
        return self();
    }

    /**
     * Sets the program dimension.
     *
     * @param programDimension the program dimension
     * @return this builder
     */
    public QuestionBuilder withProgramDimension(ProgramDimension programDimension) {
        this.programDimension = programDimension;
        return self();
    }

    /**
     * Sets the scope of focus.
     *
     * @param scopeOfFocus the scope of focus
     * @return this builder
     */
    public QuestionBuilder withScopeOfFocus(ScopeOfFocus scopeOfFocus) {
        this.scopeOfFocus = scopeOfFocus;
        return self();
    }

    /**
     * Sets the question text.
     *
     * @param questionText the question text
     * @return this builder
     */
    public QuestionBuilder withQuestionText(String questionText) {
        this.questionText = questionText;
        return self();
    }

    /**
     * Sets the choices for a multiple choice question.
     *
     * @param choices the list of choices
     * @return this builder
     */
    public QuestionBuilder withChoices(List<String> choices) {
        this.choices = choices != null ? new ArrayList<>(choices) : null;
        return self();
    }

    /**
     * Sets the choices for a multiple choice question.
     *
     * @param choices the choices as varargs
     * @return this builder
     */
    public QuestionBuilder withChoices(String... choices) {
        this.choices = choices != null ? new ArrayList<>(Arrays.asList(choices)) : null;
        return self();
    }

    /**
     * Adds a choice to the list of choices.
     *
     * @param choice the choice to add
     * @return this builder
     */
    public QuestionBuilder addChoice(String choice) {
        if (this.choices == null) {
            this.choices = new ArrayList<>();
        }
        this.choices.add(choice);
        return self();
    }

    /**
     * Sets the correct answers.
     *
     * @param answers the list of correct answers
     * @return this builder
     */
    public QuestionBuilder withCorrectAnswers(List<String> answers) {
        this.correctAnswers = answers != null ? new ArrayList<>(answers) : null;
        return self();
    }

    /**
     * Sets the correct answers.
     *
     * @param answers the answers as varargs
     * @return this builder
     */
    public QuestionBuilder withCorrectAnswers(String... answers) {
        this.correctAnswers = answers != null ? new ArrayList<>(Arrays.asList(answers)) : null;
        return self();
    }

    /**
     * Adds a correct answer.
     *
     * @param answer the answer to add
     * @return this builder
     */
    public QuestionBuilder addCorrectAnswer(String answer) {
        if (this.correctAnswers == null) {
            this.correctAnswers = new ArrayList<>();
        }
        this.correctAnswers.add(answer);
        return self();
    }

    private void validate() {
        if (finder == null) {
            throw new IllegalArgumentException("Finder not set.");
        }
        if (questionType == null) {
            throw new IllegalArgumentException("QuestionType not set.");
        }
        if (programDimension == null) {
            throw new IllegalArgumentException("ProgramDimension not set.");
        }
        if (scopeOfFocus == null) {
            throw new IllegalArgumentException("ScopeOfFocus not set.");
        }
    }

    @Override
    public Question build() {
        validate();
        return new Question(this);
    }

    // Getters

    public QuestionFinder getFinder() {
        return finder;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public ProgramDimension getProgramDimension() {
        return programDimension;
    }

    public ScopeOfFocus getScopeOfFocus() {
        return scopeOfFocus;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getChoices() {
        return choices;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }
}
