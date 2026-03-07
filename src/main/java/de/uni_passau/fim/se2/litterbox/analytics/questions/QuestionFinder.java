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

import de.uni_passau.fim.se2.litterbox.analytics.Finder;

/**
 * Interface for finders that generate comprehension questions from Scratch programs.
 *
 * <p>Extends {@link Finder} with question-specific methods for question type,
 * program dimension, and scope of focus. Question finders produce {@link Question}
 * objects via {@link AbstractQuestionFinder}.
 */
public interface QuestionFinder extends Finder<Question> {

    /**
     * Returns the type of answer expected for questions from this finder.
     *
     * @return the question type (MULTIPLE_CHOICE, FREE_TEXT, NUMBER, or YES_NO)
     */
    QuestionType getQuestionType();

    /**
     * Returns the program dimension this finder's questions focus on.
     *
     * @return the program dimension (TEXT, EXECUTION, or PURPOSE)
     */
    ProgramDimension getProgramDimension();

    /**
     * Returns the scope of focus for questions from this finder.
     *
     * @return the scope of focus (ATOM, BLOCK, RELATION, or MACRO)
     */
    ScopeOfFocus getScopeOfFocus();
}
