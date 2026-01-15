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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingWaitUntilCondition;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the MissingWaitUntilCondition bug.
 *
 * <p>The bug is reported when a WaitUntil has an unspecified boolean expression.
 * The bug is fixed if the condition is now specified.
 */
public class MissingWaitUntilConditionFix extends AbstractFixHeuristic {

    private final MissingWaitUntilCondition relatedBugFinder = new MissingWaitUntilCondition();

    public MissingWaitUntilConditionFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(WaitUntil node) {
        if (matchesBugLocation(node)) {
            // Fix: Condition IS specified (not UnspecifiedBoolExpr).
            if (!(node.getUntil() instanceof UnspecifiedBoolExpr)) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}
