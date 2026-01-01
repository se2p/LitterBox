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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Set;

/**
 * Interface for fix heuristics that check whether a previously reported bug
 * has been fixed in a program.
 *
 * <p>Unlike {@link de.uni_passau.fim.se2.litterbox.analytics.IssueFinder} which
 * finds problems, a FixHeuristic confirms that a known problem has been resolved.
 *
 * <p>Each FixHeuristic is constructed with an {@link IssueDTO} representing the
 * original bug, and checks if that specific bug has been fixed in the given program.
 */
public interface FixHeuristic {

    /**
     * Checks if the original bug has been fixed in the given program.
     *
     * @param program the program to check
     * @return a set of fix results (typically 0 or 1 elements)
     */
    Set<FixResult> check(Program program);

    /**
     * Returns the original issue that this heuristic is checking for a fix.
     *
     * @return the original issue DTO
     */
    IssueDTO getOriginalIssue();

    /**
     * Returns the name of this fix heuristic.
     *
     * <p>By convention, this is the same as the related bug finder's name.
     *
     * @return the heuristic name
     */
    String getName();
}
