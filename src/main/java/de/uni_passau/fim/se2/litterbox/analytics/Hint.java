/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import com.google.common.base.CharMatcher;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

public class Hint {

    public static final char ESCAPE_CHARACTER = '%';

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
    public static final String METHOD = "METHOD";

    private String hintKey;
    private String hintText;

    public Hint(String key) {
        this.hintKey = key;
        this.hintText = IssueTranslator.getInstance().getHint(hintKey);
    }

    public void setParameter(String key, String value) {
        hintText = hintText.replace("" + ESCAPE_CHARACTER + key, value);
    }

    public int getNumParameters() {
        return CharMatcher.is(ESCAPE_CHARACTER).countIn(hintText);
    }

    public boolean hasParameters() {
        return hintText.indexOf(ESCAPE_CHARACTER) >= 0;
    }

    public String getHintKey() {
        return hintKey;
    }

    public String getHintText() {
        return hintText;
    }
}
