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

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.SinglePower;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;
import static java.lang.Math.abs;

public class MotorLowPower extends AbstractRobotFinder {
    private static final double MOTOR_MIN_VALUE = 25;
    private static final String NAME = "motor_low_power";

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        super.visit(script);
    }

    @Override
    public void visit(RobotMoveStmt node) {
        if (robot == MCORE) {
            NumValueVisitor calc = new NumValueVisitor();
            try {
                if (node instanceof MoveSides moveSides) {
                    NumExpr powerLeft = moveSides.getLeftPower();
                    NumExpr powerRight = moveSides.getRightPower();
                    double powerLeftValue = abs(calc.calculateEndValue(powerLeft));
                    double powerRightValue = abs(calc.calculateEndValue(powerRight));
                    if (powerLeftValue < MOTOR_MIN_VALUE && powerLeftValue != 0 || powerRightValue < MOTOR_MIN_VALUE && powerRightValue != 0) {
                        addIssue(node, IssueSeverity.MEDIUM);
                    }
                } else if (node instanceof SinglePower singlePower) {
                    NumExpr power = singlePower.getPercent();
                    double powerValue = abs(calc.calculateEndValue(power));
                    if (powerValue < MOTOR_MIN_VALUE && powerValue != 0) {
                        addIssue(node, IssueSeverity.MEDIUM);
                    }
                }
            } catch (Exception ignored) {
            }
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
