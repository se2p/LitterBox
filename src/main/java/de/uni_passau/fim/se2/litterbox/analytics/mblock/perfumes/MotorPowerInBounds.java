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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.SinglePower;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.LOW;

public class MotorPowerInBounds extends AbstractRobotFinder {

    private static final String NAME = "motor_power_in_bounds";
    private static final double MOTOR_MAX_VALUE = 100;
    private static final double MCORE_MIN_VALUE = 0;

    @Override
    public void visit(Script script) {
        ignoreLooseBlocks = true;
        super.visit(script);
    }

    @Override
    public void visit(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveSides moveSides) {
                NumExpr powerLeft = moveSides.getLeftPower();
                NumExpr powerRight = moveSides.getRightPower();
                double powerLeftValue = calc.calculateEndValue(powerLeft);
                double powerRightValue = calc.calculateEndValue(powerRight);
                if (powerLeftValue <= MOTOR_MAX_VALUE && powerLeftValue >= MCORE_MIN_VALUE
                        && powerRightValue < MOTOR_MAX_VALUE && powerRightValue >= MCORE_MIN_VALUE) {
                    addIssue(node, LOW);
                }
            } else if (node instanceof SinglePower singlePower) {
                NumExpr power = singlePower.getPercent();
                double powerValue = calc.calculateEndValue(power);
                if (powerValue <= MOTOR_MAX_VALUE && powerValue >= MCORE_MIN_VALUE) {
                    addIssue(node, LOW);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
