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
package de.uni_passau.fim.se2.litterbox.utils;

/**
 * Utility class for formatting numbers in a user-friendly way.
 */
public final class NumberFormatUtil {

    private NumberFormatUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats a double value, removing unnecessary decimal places for whole numbers.
     * For example, 5.0 becomes "5", but 5.5 remains "5.5".
     *
     * @param value the value to format
     * @return the formatted string representation
     */
    public static String format(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Formats a number, removing unnecessary decimal places for whole numbers.
     *
     * @param value the value to format
     * @return the formatted string representation
     */
    public static String format(Number value) {
        return format(value.doubleValue());
    }
}
