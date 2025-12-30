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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;

/**
 * Fix heuristic for the MissingWaitUntilCondition bug.
 *
 * <p>The bug is reported when a WaitUntil has an unspecified boolean expression.
 * The bug is fixed if the condition is now specified.
 */
public class MissingWaitUntilConditionFix extends AbstractFixHeuristic {
    public static final String NAME = "missing_wait_until_condition_fix";

    public MissingWaitUntilConditionFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public void visit(WaitUntil node) {
        if (matchesBugLocation(node)) {
            // Fix: Condition IS specified (not UnspecifiedBoolExpr).
            if (!(node.getUntil() instanceof UnspecifiedBoolExpr)) {
                addIssue(node, node.getMetadata());
            }
        }
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
