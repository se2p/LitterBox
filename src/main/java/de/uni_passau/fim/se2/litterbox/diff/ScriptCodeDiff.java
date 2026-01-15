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

/**
 * Represents the diff of a single script with before/after code and annotated versions.
 */
public record ScriptCodeDiff(
        String beforeCode,
        String afterCode,
        ScriptChangeType changeType,
        String annotatedBeforeCode,
        String annotatedAfterCode
) {
    /**
     * Constructor for backward compatibility - computes annotated versions automatically.
     */
    public ScriptCodeDiff(String beforeCode, String afterCode, ScriptChangeType changeType) {
        this(beforeCode, afterCode, changeType,
                DiffAnnotator.annotateRemoved(beforeCode, afterCode),
                DiffAnnotator.annotateAdded(beforeCode, afterCode));
    }

    public boolean isUnchanged() {
        return changeType == ScriptChangeType.UNCHANGED;
    }
}
