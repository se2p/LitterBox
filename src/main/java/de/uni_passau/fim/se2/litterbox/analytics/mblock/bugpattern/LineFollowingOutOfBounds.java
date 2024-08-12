/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectLinePort;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class LineFollowingOutOfBounds extends AbstractRobotFinder {
    private static final String NAME = "line_following_out_of_bounds";
    private static final int LINE_FOLLOWING_BOTH_WHITE = 3;
    private static final int LINE_FOLLOWING_RIGHT_BLACK = 2;
    private static final int LINE_FOLLOWING_LEFT_BLACK = 1;
    private static final int LINE_FOLLOWING_BOTH_BLACK = 0;
    private boolean insideEquals;
    private boolean hasLineFollowing;
    private double sensorValue;
    private boolean setValue;

    @Override
    public void visit(Equals node) {
        insideEquals = true;
        setValue = false;
        visitChildren(node);
        if (setValue && hasLineFollowing) {
            if (!(sensorValue == LINE_FOLLOWING_BOTH_BLACK || sensorValue == LINE_FOLLOWING_LEFT_BLACK
                    || sensorValue == LINE_FOLLOWING_RIGHT_BLACK || sensorValue == LINE_FOLLOWING_BOTH_WHITE)) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideEquals = false;
        hasLineFollowing = false;
    }

    @Override
    public void visit(DetectLinePort node) {
        if (insideEquals) {
            hasLineFollowing = true;
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideEquals && !setValue) {
            setValue = true;
            sensorValue = node.getValue();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
