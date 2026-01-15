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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.BlockingIfElse;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the BlockingIfElse bug.
 *
 * <p>The bug is reported when an if-else has stop statements in both branches,
 * blocking any code after it.
 * The bug is fixed if at least one branch no longer ends with a stop.
 */
public class BlockingIfElseFix extends AbstractFixHeuristic {

    private final BlockingIfElse relatedBugFinder = new BlockingIfElse();

    public BlockingIfElseFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(IfElseStmt ifElse) {
        if (matchesBugLocation(ifElse)) {
            boolean stopsThen = BlockingIfElse.isBlocking(ifElse.getThenStmts().getStmts());
            boolean stopsElse = BlockingIfElse.isBlocking(ifElse.getElseStmts().getStmts());

            // Fixed if NOT (stops in then AND stops in else)
            if (!(stopsThen && stopsElse)) {
                addFix(ifElse, ifElse.getMetadata());
            }
        }
        super.visit(ifElse);
    }
}
