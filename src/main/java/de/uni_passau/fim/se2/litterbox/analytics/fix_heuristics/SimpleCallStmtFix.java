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

import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Abstract base class for fix heuristics that simply check if a CallStmt
 * no longer triggers the related bug finder.
 *
 * <p>This consolidates the common pattern used by:
 * <ul>
 *   <li>{@link CustomBlockWithForeverFix}</li>
 *   <li>{@link CustomBlockWithTerminationFix}</li>
 * </ul>
 *
 * <p>The fix detection logic is: if the CallStmt matching the bug location
 * no longer triggers the bug finder, report a fix.
 */
public abstract class SimpleCallStmtFix extends AbstractFixHeuristic {

    protected SimpleCallStmtFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node) && !isBugStillPresent(node)) {
            addFix(node, node.getMetadata());
        }
        visitChildren(node);
    }
}
