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
package de.uni_passau.fim.se2.litterbox.injection.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class to generate realistic typos for bug injection.
 */
public class TypoGenerator {

    /**
     * Number of available typo generation strategies.
     */
    private static final int NUM_STRATEGIES = 5;

    /**
     * Maximum number of attempts to generate a typo before falling back.
     */
    private static final int MAX_TYPO_ATTEMPTS = 10;

    private TypoGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a version of the input string with a typo.
     * If the string is too short or empty, it might return a simple variation.
     *
     * @param original The original string.
     * @return A string with a typo.
     */
    public static String generateTypo(String original) {
        if (original == null || original.isEmpty()) {
            return " ";
        }

        int strategy = ThreadLocalRandom.current().nextInt(NUM_STRATEGIES);
        int len = original.length();
        int attempt = 0;

        // Try a few times to generate a different string
        while (attempt < MAX_TYPO_ATTEMPTS) {
            String candidate = applyStrategy(original, strategy, len);
            if (!candidate.equals(original)) {
                return candidate;
            }
            strategy = (strategy + 1) % NUM_STRATEGIES;
            attempt++;
        }

        return original + " "; // Fallback
    }

    private static String applyStrategy(String original, int strategy, int len) {
        switch (strategy) {
            case 0: // Append whitespace
                return original + " ";
            case 1: // Prepend whitespace
                return " " + original;
            case 2: // Duplicate a character
                if (len > 0) {
                    int idx = ThreadLocalRandom.current().nextInt(len);
                    return original.substring(0, idx) + original.charAt(idx) + original.substring(idx);
                }
                break;
            case 3: // Delete a character
                if (len > 1) {
                    int idx = ThreadLocalRandom.current().nextInt(len);
                    return original.substring(0, idx) + original.substring(idx + 1);
                }
                break;
            case 4: // Transpose characters (swap with next)
                if (len > 1) {
                    int idx = ThreadLocalRandom.current().nextInt(len - 1);
                    return original.substring(0, idx)
                            + original.charAt(idx + 1)
                            + original.charAt(idx)
                            + original.substring(idx + 2);
                }
                break;
            default:
                break;
        }
        return original;
    }
}
