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
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to annotate code diffs with ScratchBlocks-safe markers and block-level tints.
 * Uses suffix emojis (e.g. block 🔴 :: #dc3545) to avoid breaking block icon matching.
 */
public final class DiffAnnotator {

    // Emojis as suffixes
    private static final String REMOVED_TINT = " :: #dc3545";

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
            return annotateAllLines(beforeCode, REMOVED_TINT);
        }
        return annotate(beforeCode, afterCode, REMOVED_TINT, true);
    }

    /**
     * Annotates the "after" code with markers and tints showing added/changed lines.
     */
    public static String annotateAdded(String beforeCode, String afterCode) {
        if (afterCode == null || afterCode.isEmpty()) {
            return "";
        }
        if (beforeCode == null || beforeCode.isEmpty()) {
            return annotateAllLines(afterCode, ADDED_TINT);
        }
        return annotate(beforeCode, afterCode, ADDED_TINT, false);
    }

    private static String annotate(String beforeCode, String afterCode, String tint, boolean isSource) {
        List<String> beforeLines = Arrays.asList(beforeCode.split("\n", -1));
        List<String> afterLines = Arrays.asList(afterCode.split("\n", -1));

        Patch<String> patch = DiffUtils.diff(beforeLines, afterLines);

        // Determine which lines to iterate and mark (source or target)
        List<String> linesToAnnotate = isSource ? beforeLines : afterLines;
        boolean[] marked = new boolean[linesToAnnotate.size()];

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            Chunk<String> chunk = isSource ? delta.getSource() : delta.getTarget();
            int start = chunk.getPosition();
            int size = chunk.size();
            for (int i = start; i < start + size; i++) {
                if (i < marked.length) {
                    marked[i] = true;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linesToAnnotate.size(); i++) {
            sb.append(linesToAnnotate.get(i));
            if (marked[i]) {
                sb.append(tint);
            }
            if (i < linesToAnnotate.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String annotateAllLines(String code, String tint) {
        String[] lines = code.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]).append(tint);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
