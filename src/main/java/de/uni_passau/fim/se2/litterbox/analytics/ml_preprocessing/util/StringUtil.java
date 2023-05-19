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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    private static final Pattern ALLOWED_LABEL_CHARS = Pattern.compile("[^a-z0-9_-]");

    private static final Pattern SPLIT_PATTERN = Pattern.compile(
            "(?<=[a-z])(?=[A-Z])|_|-|\\d|\\p{Punct}|(?<=[A-Z])(?=[A-Z][a-z])|\\s+"
    );

    private StringUtil() {
        throw new IllegalCallerException("utility class constructor");
    }

    public static List<String> splitToSubtokens(String str1) {
        final String str2 = str1.trim();
        return Stream.of(SPLIT_PATTERN.split(str2))
                .filter(s -> !s.isEmpty())
                .map(StringUtil::normalizeSubtoken)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Only keeps lowercase a-z characters.
     *
     * @param original Some string.
     * @return The input converted to lowercase and all non-{@code a-z}-characters removed.
     */
    private static String normalizeSubtoken(final String original) {
        return original.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    /**
     * Converts string literals as they appear in Scratch programs into a normalised form without special characters.
     *
     * <p>Applied normalisations:
     * <ul>
     *     <li>Replaces whitespace with underscores.</li>
     *     <li>Converts to lowercase.</li>
     *     <li>Removes all characters that are not alphanumeric, underscores, or dashes.</li>
     * </ul>
     *
     * @param s Some string.
     * @return The input string in its normalised form.
     */
    public static String normaliseString(final String s) {
        final String noSpaces = s.trim().replaceAll("\\s+", "_");
        final String label = ALLOWED_LABEL_CHARS.matcher(noSpaces.toLowerCase(Locale.ROOT)).replaceAll("");
        if (label.isEmpty()) {
            return "EMPTY_STRING";
        } else {
            return label;
        }
    }
}
