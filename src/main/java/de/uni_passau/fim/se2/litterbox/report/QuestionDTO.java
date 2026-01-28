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
package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;

import java.util.List;

/**
 * Data transfer object for a comprehension question with block model metadata.
 *
 * @param id               Unique identifier for the question
 * @param finder           The name of the question finder that generated this question
 * @param name             Human-readable name of the question type
 * @param questionType     The type of answer expected (MULTIPLE_CHOICE, FREE_TEXT, NUMBER, YES_NO)
 * @param programDimension The program dimension in the block model (TEXT, EXECUTION, PURPOSE)
 * @param scopeOfFocus     The scope of focus in the block model (ATOM, BLOCK, RELATION, MACRO)
 * @param sprite           The sprite/actor this question relates to
 * @param questionText     The actual question text
 * @param choices          List of choices for multiple choice questions (null for other types)
 * @param correctAnswers   List of correct answers (null for free text questions without known answers)
 * @param scratchBlocksCode The code context in scratchblocks notation
 * @param hint             Optional hint for the question
 */
public record QuestionDTO(
        int id,
        String finder,
        String name,
        QuestionType questionType,
        ProgramDimension programDimension,
        ScopeOfFocus scopeOfFocus,
        String sprite,
        String questionText,
        List<String> choices,
        List<String> correctAnswers,
        String scratchBlocksCode,
        String hint
) {
    /**
     * Returns the block model cell key (e.g., "TEXT/BLOCK").
     */
    public String getBlockModelCell() {
        return programDimension.name() + "/" + scopeOfFocus.name();
    }
}
