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
package de.uni_passau.fim.se2.litterbox.export;

/**
 * Constants for Scratch key codes used in key press events.
 *
 * <p>Scratch uses specific numeric key codes for special keys and standard
 * ASCII values for alphanumeric keys.
 */
public final class ScratchKeyCode {

    private ScratchKeyCode() {
        // Utility class - prevent instantiation
    }

    // Special keys
    public static final int SPACE = 32;
    public static final int LEFT_ARROW = 37;
    public static final int UP_ARROW = 38;
    public static final int RIGHT_ARROW = 39;
    public static final int DOWN_ARROW = 40;

    // Digit range (0-9)
    public static final int DIGIT_START = 48;
    public static final int DIGIT_END = 57;

    // Uppercase letter range (A-Z)
    public static final int UPPERCASE_START = 65;
    public static final int UPPERCASE_END = 90;

    // Offset to convert uppercase to lowercase
    public static final int UPPERCASE_TO_LOWERCASE_OFFSET = 32;

    /**
     * Checks if the key code represents a digit (0-9).
     *
     * @param keyCode The key code to check
     * @return true if the key code is a digit
     */
    public static boolean isDigit(int keyCode) {
        return keyCode >= DIGIT_START && keyCode <= DIGIT_END;
    }

    /**
     * Checks if the key code represents an uppercase letter (A-Z).
     *
     * @param keyCode The key code to check
     * @return true if the key code is an uppercase letter
     */
    public static boolean isUppercaseLetter(int keyCode) {
        return keyCode >= UPPERCASE_START && keyCode <= UPPERCASE_END;
    }

    /**
     * Converts a key code to its character representation.
     * For uppercase letters, returns the lowercase version.
     *
     * @param keyCode The key code to convert
     * @return The character, or null if not a printable character
     */
    public static Character toChar(int keyCode) {
        if (isDigit(keyCode)) {
            return (char) keyCode;
        }
        if (isUppercaseLetter(keyCode)) {
            return (char) (keyCode + UPPERCASE_TO_LOWERCASE_OFFSET);
        }
        return null;
    }
}
