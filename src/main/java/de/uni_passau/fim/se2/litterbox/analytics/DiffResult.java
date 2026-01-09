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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.diff.ProjectCodeDiff;

import java.util.List;

/**
 * Result of a comparison between two project versions.
 *
 * <p>Issues are categorized into four groups:
 * <ul>
 *   <li><b>FIXED</b>: Issues present in the before version that are confirmed fixed by heuristics</li>
 *   <li><b>NEW</b>: Issues only present in the after version</li>
 *   <li><b>PERSISTING</b>: Issues present in both versions (same block ID and finder)</li>
 *   <li><b>REMOVED</b>: Issues from before that disappeared but weren't confirmed as fixed</li>
 * </ul>
 *
 * @param beforeSource  Description/Path of source before (e.g. filename)
 * @param afterSource   Description/Path of source after
 * @param fixed         List of fixed issues
 * @param newIssues     List of new issues
 * @param persisting    List of persisting issues
 * @param removed       List of removed issues (issues in "before" that are gone but not verified fixed)
 * @param projectDiff   The code-based diff of the project
 */
public record DiffResult(
        String beforeSource,
        String afterSource,
        List<Issue> fixed,
        List<Issue> newIssues,
        List<Issue> persisting,
        List<Issue> removed,
        ProjectCodeDiff projectDiff
) {
    /**
     * Creates an empty diff result with no issues.
     */
    public static DiffResult empty(String beforeSource, String afterSource) {
        return new DiffResult(beforeSource, afterSource, List.of(), List.of(), List.of(), List.of(), ProjectCodeDiff.empty());
    }

    /**
     * Returns the total count of fixed issues.
     */
    public int fixedCount() {
        return fixed.size();
    }

    /**
     * Returns the total count of new issues.
     */
    public int newCount() {
        return newIssues.size();
    }

    /**
     * Returns the total count of persisting issues.
     */
    public int persistingCount() {
        return persisting.size();
    }

    /**
     * Returns the total count of removed issues.
     */
    public int removedCount() {
        return removed.size();
    }

    /**
     * Returns the total number of issues in the after version.
     */
    public int afterTotalCount() {
        return newIssues.size() + persisting.size();
    }

    /**
     * Returns the total number of issues in the before version.
     */
    public int beforeTotalCount() {
        return fixed.size() + persisting.size() + removed.size();
    }
}
