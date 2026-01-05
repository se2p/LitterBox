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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Set;

/**
 * Fix heuristic for the RedundantAssignment bug.
 *
 * <p>The bug is reported when a variable or attribute is assigned a value
 * that is immediately overwritten without being used.
 *
 * <p>The bug is fixed if the assignment is no longer redundant (i.e., it is
 * now used before being overwritten, or the overwriting assignment was removed).
 */
public class RedundantAssignmentFix extends AbstractFixHeuristic {

    private final RedundantAssignment relatedBugFinder = new RedundantAssignment();

    public RedundantAssignmentFix(IssueDTO originalIssue) {
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

    @Override
    protected boolean isBugStillPresent(ASTNode node) {
        String originalBlockId = getBugLocationBlockId();

        for (de.uni_passau.fim.se2.litterbox.analytics.Issue issue : currentBugInstances) {
            // If the issue is only "possible" redundant, we consider the "always" redundant bug fixed.
            if (RedundantAssignment.HINT_POSSIBLE.equals(issue.getHint().getKey())) {
                continue;
            }

            ASTNode bugLoc = issue.getCodeLocation();

            if (originalBlockId != null && !originalBlockId.isBlank()) {
                if (AstNodeUtil.hasBlockId(bugLoc, originalBlockId)) {
                    return true;
                }
            }

            if (isSelfOrDescendant(bugLoc, node)) {
                return true;
            }
        }
        return false;
    }
}
