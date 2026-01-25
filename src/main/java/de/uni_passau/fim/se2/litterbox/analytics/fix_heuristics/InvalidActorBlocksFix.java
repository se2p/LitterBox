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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidActorBlocks;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Set;

/**
 * Fix heuristic for the InvalidActorBlocks bug.
 *
 * <p>The bug is considered fixed if:
 * <ul>
 *   <li>The invalid block was deleted, or</li>
 *   <li>The block was moved to the correct actor type (stage vs sprite)</li>
 * </ul>
 */
public class InvalidActorBlocksFix extends AbstractFixHeuristic {

    private final InvalidActorBlocks relatedBugFinder = new InvalidActorBlocks();

    public InvalidActorBlocksFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        return checkWithNodeFinder(program);
    }
}
