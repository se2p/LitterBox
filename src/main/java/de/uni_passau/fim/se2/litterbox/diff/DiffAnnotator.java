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
package de.uni_passau.fim.se2.litterbox.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to annotate code diffs with ScratchBlocks-safe markers and block-level tints.
 * Uses suffix emojis (e.g. block 🔴 :: #dc3545) to avoid breaking block icon matching.
 */
public final class DiffAnnotator {

    // Emojis as suffixes
    private static final String REMOVED_MARKER = " 🔴";
    private static final String REMOVED_TINT = " :: #dc3545";

    private static final String ADDED_MARKER = " 🟢";
    private static final String ADDED_TINT = " :: #28a745";

    private DiffAnnotator() {
        // Utility class
    }

    /**
     * Annotates the "before" code with markers and tints showing deleted/changed lines.
     */
    public static String annotateRemoved(String beforeCode, String afterCode) {
        if (beforeCode == null || beforeCode.isEmpty()) {
            return "";
        }
        if (afterCode == null || afterCode.isEmpty()) {
            return annotateAllLines(beforeCode, REMOVED_MARKER, REMOVED_TINT);
        }

        List<String> beforeLines = Arrays.asList(beforeCode.split("\n", -1));
        List<String> afterLines = Arrays.asList(afterCode.split("\n", -1));

        Patch<String> patch = DiffUtils.diff(beforeLines, afterLines);
        boolean[] deleted = new boolean[beforeLines.size()];
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int start = delta.getSource().getPosition();
            int size = delta.getSource().size();
            for (int i = start; i < start + size; i++) {
                if (i < deleted.length) {
                    deleted[i] = true;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < beforeLines.size(); i++) {
            sb.append(beforeLines.get(i));
            if (deleted[i]) {
                sb.append(REMOVED_MARKER).append(REMOVED_TINT);
            }
            if (i < beforeLines.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Annotates the "after" code with markers and tints showing added/changed lines.
     */
    public static String annotateAdded(String beforeCode, String afterCode) {
        if (afterCode == null || afterCode.isEmpty()) {
            return "";
        }
        if (beforeCode == null || beforeCode.isEmpty()) {
            return annotateAllLines(afterCode, ADDED_MARKER, ADDED_TINT);
        }

        List<String> beforeLines = Arrays.asList(beforeCode.split("\n", -1));
        List<String> afterLines = Arrays.asList(afterCode.split("\n", -1));

        Patch<String> patch = DiffUtils.diff(beforeLines, afterLines);
        boolean[] added = new boolean[afterLines.size()];
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int start = delta.getTarget().getPosition();
            int size = delta.getTarget().size();
            for (int i = start; i < start + size; i++) {
                if (i < added.length) {
                    added[i] = true;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < afterLines.size(); i++) {
            sb.append(afterLines.get(i));
            if (added[i]) {
                sb.append(ADDED_MARKER).append(ADDED_TINT);
            }
            if (i < afterLines.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String annotateAllLines(String code, String marker, String tint) {
        String[] lines = code.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]).append(marker).append(tint);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
