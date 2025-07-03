/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
 * Formats the hint text by replacing the special tags.
 *
 * <p>Special tags:
 * <ul>
 *     <li>{@code [b]}/{@code [/b]}: bold</li>
 *     <li>{@code [newLine]}: line break</li>
 *     <li>{@code [sbi]}/{@code [/sbi]}: ScratchBlocks code snippet</li>
 *     <li>{@code [bc]}/{@code [/bc]}: highlighted text</li>
 *     <li>{@code [var]}/{@code [/var]}: round variable</li>
 *     <li>{@code [list]}/{@code [/list]}: round list variable</li>
 * </ul>
 */
public final class HintTextFormatter {

    private HintTextFormatter() {
        // intentionally empty, utility class
    }

    /**
     * Replaces the special tags in hint texts to create Markdown.
     *
     * @param hintText The original hint text.
     * @return The Markdown-formatted hint text.
     */
    public static String formatHintTextMarkdown(final String hintText) {
        return hintText
                .replace("[b]", "**")
                .replace("[/b]", "**")
                .replace("[newLine]", "\n")
                .replace("[sbi]", "```")
                .replace("[sbi]", "```")
                .replace("[bc]", "*")
                .replace("[/bc]", "*")
                .replace("[var][/var]", "(variable)")
                .replace("[list][/list]", "(list)")
                .replace("[var]", "(")
                .replace("[/var]", ")")
                .replace("[list]", "(")
                .replace("[/list]", ")");
    }
}
