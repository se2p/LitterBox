/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.DetectLinePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.MBlockNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.RobotTimer;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.MEDIUM;

public class SensorValueEquals extends AbstractRobotFinder {

    private static final String NAME = "sensor_value_equals";
    private boolean inEquals = false;

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        super.visit(script);
    }

    @Override
    public void visit(Equals node) {
        boolean nestedEquals = inEquals;    // for if some idiot nests equals in equals...
        inEquals = true;

        visitChildren(node);

        inEquals = nestedEquals;
    }

    @Override
    public void visit(MBlockNumExpr node) {
        if (inEquals) {
            if (!(node instanceof DetectLinePort) && !(node instanceof RobotTimer)) {
                addIssue(node, MEDIUM);
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
