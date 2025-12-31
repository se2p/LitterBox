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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Set;

/**
 * Fix heuristic for the CustomBlockWithTermination bug.
 *
 * <p>The bug is reported when a call to a procedure containing StopAll/DeleteClone
 * is not in the last position, blocking subsequent code.
 * The bug is fixed if the procedure no longer terminates, or the call is now last.
 */
public class CustomBlockWithTerminationFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new CustomBlockWithTermination();

    public CustomBlockWithTerminationFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> check(Program program) {
        refreshBugInstances(program);
        return super.check(program);
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node) && !isBugStillPresent(node)) {
            addIssue(node, node.getMetadata());
        }
        super.visit(node);
    }
}
