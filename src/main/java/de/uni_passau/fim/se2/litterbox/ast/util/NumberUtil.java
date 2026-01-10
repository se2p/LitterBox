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
package de.uni_passau.fim.se2.litterbox.ast.util;

import java.util.regex.Pattern;

/**
 * Utility class for common number-related operations and regex patterns.
 */
public final class NumberUtil {

    /**
     * Regex for characters allowed in a string that "looks like" a number.
     */
    public static final Pattern ALLOWED_CHARS = Pattern.compile("^[0-9eE.+\\-]+$");

    /**
     * Regex for a valid numeric string: optional sign, integer or decimal part, optional scientific notation.
     */
    public static final Pattern VALID_NUMBER = Pattern.compile("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$");

    private NumberUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Checks if a string looks like a number (contains only digit-like characters) but is not a valid number.
     *
     * @param text The string to check.
     * @return {@code true} if it's an invalid number string, {@code false} otherwise.
     */
    public static boolean isInvalidNumberString(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        return ALLOWED_CHARS.matcher(trimmed).matches() && !VALID_NUMBER.matcher(trimmed).matches();
    }

    /**
     * Checks if a string looks like it was intended to be a number.
     *
     * @param text The string to check.
     * @return {@code true} if it's either a valid number or an invalid number string.
     */
    public static boolean isNumberLike(String text) {
        if (text == null) {
            return false;
        }
        return ALLOWED_CHARS.matcher(text.trim()).matches();
    }
}
