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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_passau.fim.se2.litterbox.analytics.HintPlaceholder;
import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * Represents translatable question text for comprehension questions.
 *
 * <p>Question texts are loaded from {@code QuestionTexts_*.properties} files and can contain
 * parameterized placeholders that are substituted at render time.
 *
 * <p>Common placeholders include:
 * <ul>
 *   <li>{@code %VARIABLE} - Variable name</li>
 *   <li>{@code %CONDITION} - Condition expression</li>
 *   <li>{@code %STATEMENT} - Statement representation</li>
 *   <li>{@code %EVENT} - Event description</li>
 *   <li>{@code %VALUE} - A value (numeric or string)</li>
 *   <li>{@code %ATTRIBUTE} - An attribute name (e.g., X position)</li>
 * </ul>
 *
 * @see TranslatableText
 */
public class QuestionText extends TranslatableText {

    // Common placeholder constants (shared with Hint for consistency)
    public static final String VARIABLE = "VARIABLE";
    public static final String CONDITION = "CONDITION";
    public static final String STATEMENT = "STATEMENT";
    public static final String EVENT = "EVENT";
    public static final String ACTOR = "ACTOR";
    public static final String ATTRIBUTE = "ATTRIBUTE";
    public static final String MESSAGE = "MESSAGE";
    public static final String VALUE = "VALUE";
    public static final String NEXT_BLOCK = "NEXT_BLOCK";
    public static final String OPERATOR = "OPERATOR";
    public static final String OP1 = "OP1";
    public static final String SUBJECT = "SUBJECT";
    public static final String BLOCK = "BLOCK";
    public static final String ALL_SPRITES = "all_sprites";
    public static final String STAGE = "stage";

    // Translation keys for common answer terms
    public static final String YES_TERM = "yes_term";
    public static final String NO_TERM = "no_term";
    public static final String TRUE_VALUE = "true_value";
    public static final String FALSE_VALUE = "false_value";

    @JsonCreator
    private QuestionText(
            @JsonProperty("key") String key,
            @JsonProperty("textContent") String text) {
        super(key, text);
    }

    /**
     * Creates a QuestionText from a key for lookup in QuestionTexts properties.
     *
     * @param key the key to look up (typically the finder name)
     * @return a new QuestionText instance
     */
    public static QuestionText fromKey(final String key) {
        return new QuestionText(key, null);
    }

    /**
     * Creates a QuestionText from pre-translated text.
     *
     * @param text the pre-translated text
     * @return a new QuestionText instance
     */
    public static QuestionText fromText(final String text) {
        return new QuestionText(null, text);
    }

    /**
     * Returns the question text with parameters substituted.
     *
     * @param translator the translator for lookups
     * @return the resolved question text
     */
    public String getQuestionText(final IssueTranslator translator) {
        return resolveText(translator, translator::getQuestionText);
    }

    /**
     * Sets a parameter on this question text and returns this instance.
     *
     * @param key   the parameter name
     * @param value the parameter value
     * @return this instance
     */
    public QuestionText withQuestionParameter(String key, String value) {
        setParameter(key, value);
        return this;
    }

    /**
     * Sets a translatable parameter on this question text and returns this instance.
     *
     * @param key   the parameter name
     * @param value the translatable parameter value
     * @return this instance
     */
    public QuestionText withQuestionParameter(String key, HintPlaceholder value) {
        setParameter(key, value);
        return this;
    }

    /**
     * Sets a translatable key parameter on this question text and returns this instance.
     *
     * @param key   the parameter name
     * @param value the translatable text containing the key
     * @return this instance
     */
    public QuestionText withQuestionParameter(String key, TranslatableText value) {
        setParameter(key, new HintPlaceholder.Translatable(value.getKey()));
        return this;
    }
}
