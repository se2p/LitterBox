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
package de.uni_passau.fim.se2.litterbox.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * Represents translatable hint text for issues.
 *
 * <p>Hints are loaded from {@code IssueHints_*.properties} files and can contain
 * parameterized placeholders that are substituted at render time.
 *
 * @see TranslatableText
 */
public class Hint extends TranslatableText {

    // Common placeholder constants for hint text
    public static final String HINT_SPRITE = "SPRITE";
    public static final String HINT_SPRITES = "SPRITES";
    public static final String HINT_VARIABLE = "VARIABLE";
    public static final String HINT_VARIABLE1 = "VARIABLE1";
    public static final String HINT_VARIABLE2 = "VARIABLE2";
    public static final String HINT_MESSAGE = "MESSAGE";
    public static final String HINT_KEY = "KEY";
    public static final String HINT_SAY_THINK = "SAYTHINK";
    public static final String THEN_ELSE = "THENELSE";
    public static final String BLOCK_NAME = "BLOCKNAME";
    public static final String EVENT_HANDLER = "EVENTHANDLER";
    public static final String EVENT = "EVENT";
    public static final String STATEMENT = "STATEMENT";
    public static final String PROCEDURE = "PROCEDURE";
    public static final String METHOD = "METHOD";
    public static final String HINT_MESSAGE_MIDDLE = "MESSAGE_MIDDLE";
    public static final String HINT_MESSAGE_FINAL = "MESSAGE_FINAL";
    public static final String HINT_BLOCKNAME_MIDDLE = "BLOCKNAME_MIDDLE";
    public static final String HINT_BLOCKNAME_FINAL = "BLOCKNAME_FINAL";
    public static final String CONDITION = "CONDITION";
    public static final String ACTOR = "ACTOR";
    public static final String CHOICES = "CHOICES";
    public static final String ANSWER = "ANSWER";

    @JsonCreator
    private Hint(
            @JsonProperty("key") String hintKey,
            @JsonProperty("textContent") String hintText) {
        super(hintKey, hintText);
    }

    /**
     * Creates a Hint from a key for lookup in IssueHints properties.
     *
     * @param hintKey the key to look up
     * @return a new Hint instance
     */
    public static Hint fromKey(final String hintKey) {
        return new Hint(hintKey, null);
    }

    /**
     * Creates a Hint from pre-translated text.
     *
     * @param hintText the pre-translated text
     * @return a new Hint instance
     */
    public static Hint fromText(final String hintText) {
        return new Hint(null, hintText);
    }

    /**
     * Returns the hint text with parameters substituted.
     * This is the legacy API name, equivalent to getText().
     *
     * @param translator the translator for lookups
     * @return the resolved hint text
     */
    public String getHintText(final IssueTranslator translator) {
        return resolveText(translator, translator::getHint);
    }

    @Override
    protected boolean useFormattingTags() {
        return true;
    }
}
