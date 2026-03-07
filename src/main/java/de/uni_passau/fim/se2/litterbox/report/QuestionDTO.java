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

import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionText;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.List;

/**
 * Data transfer object for a comprehension question with block model metadata.
 *
 * <p>
 * Stores choices and answers as {@link TranslatableText} for proper i18n support
 * and rendering with formatting tags. Use {@link #getChoices(IssueTranslator)} and
 * {@link #getCorrectAnswers(IssueTranslator)} to resolve to display strings.
 *
 * @param id               Unique identifier for the question
 * @param finder           The name of the question finder that generated this question
 * @param name             Human-readable name of the question type
 * @param questionType     The type of answer expected (MULTIPLE_CHOICE, FREE_TEXT, NUMBER, YES_NO)
 * @param programDimension The program dimension in the block model (TEXT, EXECUTION, PURPOSE)
 * @param scopeOfFocus     The scope of focus in the block model (ATOM, BLOCK, RELATION, MACRO)
 * @param sprite           The sprite/actor this question relates to
 * @param questionText     The actual question text
 * @param translatableQuestionText The translatable question text object for re-resolution (may be null)
 * @param translatableChoices List of choices as TranslatableText objects (null for non-MC questions)
 * @param translatableCorrectAnswers List of correct answers as TranslatableText objects
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
        TranslatableText translatableQuestionText,
        List<TranslatableText> translatableChoices,
        List<TranslatableText> translatableCorrectAnswers,
        String scratchBlocksCode,
        String hint
) {
    /**
     * Returns the question text re-resolved using the given translator.
     * Falls back to the pre-resolved {@code questionText} if no translatable object is available.
     *
     * @param translator the translator for resolving text
     * @return the resolved question text
     */
    public String getResolvedQuestionText(IssueTranslator translator) {
        if (translatableQuestionText instanceof QuestionText qt) {
            return qt.getQuestionText(translator);
        }
        return questionText;
    }

    /**
     * Returns the block model cell key (e.g., "TEXT/BLOCK").
     */
    public String getBlockModelCell() {
        return programDimension.name() + "/" + scopeOfFocus.name();
    }

    /**
     * Returns choices resolved to strings using the given translator.
     *
     * @param translator the translator for resolving text
     * @return list of resolved choice strings, or null if no choices
     */
    public List<String> getChoices(IssueTranslator translator) {
        return resolveTextList(translatableChoices, translator);
    }

    /**
     * Returns correct answers resolved to strings using the given translator.
     *
     * @param translator the translator for resolving text
     * @return list of resolved answer strings, or null if no answers
     */
    public List<String> getCorrectAnswers(IssueTranslator translator) {
        return resolveTextList(translatableCorrectAnswers, translator);
    }

    private static List<String> resolveTextList(List<TranslatableText> texts,
                                                IssueTranslator translator) {
        if (texts == null) {
            return null;
        }
        return texts.stream()
                .map(t -> t.resolveText(translator, translator::getInfo, false))
                .toList();
    }
}
