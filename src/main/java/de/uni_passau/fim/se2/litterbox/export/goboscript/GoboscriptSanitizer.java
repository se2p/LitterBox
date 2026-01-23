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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for sanitizing identifiers to ensure they are valid Goboscript names.
 */
public final class GoboscriptSanitizer {

    private GoboscriptSanitizer() {
        // Prevent instantiation
    }

    /**
     * Sanitizes a string to be a valid Goboscript identifier.
     * Replaces invalid characters, ensures it starts with a letter or underscore,
     * and avoids reserved keywords.
     *
     * @param name The original identifier name.
     * @return A valid Goboscript identifier.
     */
    public static String escapeIdentifier(String name) {
        // Replace German umlauts (these expand to two characters, so handle before stripAccents)
        name = name.replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue")
                .replace("ß", "ss");

        // Strip accents from remaining characters (á→a, é→e, ñ→n, etc.)
        name = StringUtils.stripAccents(name);

        // Use a StringBuilder to construct the sanitized name
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // Only allow ASCII letters, digits, and underscores (Goboscript doesn't support Unicode identifiers)
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }

        String result = sb.toString();

        // Ensure it doesn't start with a digit (Goboscript identifiers must start with letter or underscore)
        if (!result.isEmpty() && Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }

        // Fallback for empty or underscore-only strings (Goboscript requires at least one letter)
        if (result.isEmpty() || result.chars().allMatch(c -> c == '_')) {
            return "var_" + Math.abs(name.hashCode());
        }

        // Handle keywords
        if (GoboscriptKeywords.KEYWORDS.contains(result.toLowerCase())) {
            return "_" + result;
        }

        return result;
    }

    /**
     * Escapes a list identifier name with a "_list" suffix to avoid name collisions
     * with variables of the same name in Scratch.
     *
     * @param name The original list name.
     * @return A sanitized list identifier.
     */
    public static String escapeListIdentifier(String name) {
        return escapeIdentifier(name) + "_list";
    }

    /**
     * Escapes a procedure name, adding a "_proc" suffix if it conflicts with a
     * Goboscript built-in command name.
     *
     * @param name The original procedure name.
     * @return A sanitized procedure identifier.
     */
    public static String escapeProcedureName(String name) {
        String escaped = escapeIdentifier(name);
        if (GoboscriptKeywords.RESERVED_COMMANDS.contains(escaped.toLowerCase())) {
            return escaped + "_proc";
        }
        return escaped;
    }
}
