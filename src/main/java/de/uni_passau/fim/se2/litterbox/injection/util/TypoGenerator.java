package de.uni_passau.fim.se2.litterbox.injection.util;

import java.util.Random;

/**
 * Utility class to generate realistic typos for bug injection.
 */
public class TypoGenerator {

    private static final Random RANDOM = new Random();

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

        int strategy = RANDOM.nextInt(5);
        int len = original.length();
        int attempt = 0;

        // Try a few times to generate a different string
        while (attempt < 10) {
            String candidate = applyStrategy(original, strategy, len);
            if (!candidate.equals(original)) {
                return candidate;
            }
            strategy = (strategy + 1) % 5;
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
                    int idx = RANDOM.nextInt(len);
                    return original.substring(0, idx) + original.charAt(idx) + original.substring(idx);
                }
                break;
            case 3: // Delete a character
                if (len > 1) {
                    int idx = RANDOM.nextInt(len);
                    return original.substring(0, idx) + original.substring(idx + 1);
                }
                break;
            case 4: // Transpose characters (swap with next)
                if (len > 1) {
                    int idx = RANDOM.nextInt(len - 1);
                    return original.substring(0, idx) +
                            original.charAt(idx + 1) +
                            original.charAt(idx) +
                            original.substring(idx + 2);
                }
                break;
        }
        return original;
    }
}
