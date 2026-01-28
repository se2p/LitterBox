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

import de.uni_passau.fim.se2.litterbox.analytics.Finding;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.Collections;
import java.util.List;

/**
 * Represents a comprehension question generated from a Scratch project.
 * Extends {@link Finding} with question-specific fields like choices, answers, and question type.
 */
public class Question extends Finding {

    private final QuestionFinder finder;
    private final QuestionType questionType;
    private final ProgramDimension programDimension;
    private final ScopeOfFocus scopeOfFocus;
    private final String questionText;
    private final List<String> choices;
    private final List<String> correctAnswers;

    /**
     * Creates a Question from a builder.
     *
     * @param builder the question builder
     */
    public Question(QuestionBuilder builder) {
        super(builder.getProgram(), builder.getActor(), builder.getScript(),
              builder.getCurrentNode(), builder.getMetadata(), builder.getId());
        this.finder = builder.getFinder();
        this.questionType = builder.getQuestionType();
        this.programDimension = builder.getProgramDimension();
        this.scopeOfFocus = builder.getScopeOfFocus();
        this.questionText = builder.getQuestionText();
        this.choices = builder.getChoices() != null
                ? Collections.unmodifiableList(builder.getChoices())
                : Collections.emptyList();
        this.correctAnswers = builder.getCorrectAnswers() != null
                ? Collections.unmodifiableList(builder.getCorrectAnswers())
                : Collections.emptyList();
    }

    @Override
    public String getFinderName() {
        return finder.getName();
    }

    /**
     * Returns the finder that created this question.
     *
     * @return the question finder
     */
    public QuestionFinder getFinder() {
        return finder;
    }

    /**
     * Returns the type of answer expected for this question.
     *
     * @return the question type (MULTIPLE_CHOICE, FREE_TEXT, NUMBER, or YES_NO)
     */
    public QuestionType getQuestionType() {
        return questionType;
    }

    /**
     * Returns the program dimension this question focuses on.
     *
     * @return the program dimension (TEXT, EXECUTION, or PURPOSE)
     */
    public ProgramDimension getProgramDimension() {
        return programDimension;
    }

    /**
     * Returns the scope of focus for this question.
     *
     * @return the scope of focus (ATOM, BLOCK, RELATION, or MACRO)
     */
    public ScopeOfFocus getScopeOfFocus() {
        return scopeOfFocus;
    }

    /**
     * Returns the question text.
     *
     * @return the question text
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Returns the available choices for multiple choice questions.
     *
     * @return unmodifiable list of choices, or empty list if not applicable
     */
    public List<String> getChoices() {
        return choices;
    }

    /**
     * Returns the correct answers for this question.
     *
     * @return unmodifiable list of correct answers, or empty list if unknown
     */
    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    /**
     * Returns whether this question has choices (is multiple choice).
     *
     * @return true if choices are available
     */
    public boolean hasChoices() {
        return !choices.isEmpty();
    }

    /**
     * Returns whether this question has known correct answers.
     *
     * @return true if correct answers are known
     */
    public boolean hasCorrectAnswers() {
        return !correctAnswers.isEmpty();
    }

    /**
     * Returns the block model cell identifier (e.g., "TEXT/BLOCK").
     *
     * @return the block model cell key
     */
    public String getBlockModelCell() {
        return programDimension.name() + "/" + scopeOfFocus.name();
    }
}
