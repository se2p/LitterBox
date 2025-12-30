/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import java.util.List;

/**
 * Contains the results of a diff analysis between two versions of a Scratch project.
 *
 * <p>Issues are categorized into four groups:
 * <ul>
 *   <li><b>FIXED</b>: Issues present in the before version that are confirmed fixed by heuristics</li>
 *   <li><b>NEW</b>: Issues only present in the after version</li>
 *   <li><b>PERSISTING</b>: Issues present in both versions (same block ID and finder)</li>
 *   <li><b>REMOVED</b>: Issues from before that disappeared but weren't confirmed as fixed</li>
 * </ul>
 *
 * @param beforeSource the source identifier for the before version (file path)
 * @param afterSource the source identifier for the after version (file path)
 * @param fixed issues that were verified as fixed by fix heuristics
 * @param newIssues issues only present in the after version
 * @param persisting issues present in both versions
 * @param removed issues that disappeared but weren't verified as properly fixed
 */
public record DiffResult(
        String beforeSource,
        String afterSource,
        List<Issue> fixed,
        List<Issue> newIssues,
        List<Issue> persisting,
        List<Issue> removed
) {
    /**
     * Creates an empty diff result with no issues.
     */
    public static DiffResult empty(String beforeSource, String afterSource) {
        return new DiffResult(beforeSource, afterSource, List.of(), List.of(), List.of(), List.of());
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
