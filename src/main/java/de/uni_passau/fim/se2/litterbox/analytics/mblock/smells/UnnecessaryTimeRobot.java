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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.smells;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDColorTimed;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDColorTimedPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.FaceTimed;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.FaceTimedPort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.PlayFrequency;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;

/**
 * This finder looks if a wait block waits for 0 seconds and thus is unnecessary.
 */
public class UnnecessaryTimeRobot extends AbstractRobotFinder {
    public static final String NAME = "unnecessary_time_robot";

    @Override
    public void visit(LEDColorTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(LEDColorTimedPosition node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(FaceTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(FaceTimedPort node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(KeepBackwardTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(KeepForwardTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(MoveBackwardTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(MoveForwardTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(TurnLeftTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(TurnRightTimed node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(PlayFrequency node) {
        if (checkTime(node.getTime())) {
            addIssue(node, node.getMetadata());
        }
    }

    private boolean checkTime(NumExpr node) {
        if (node instanceof NumberLiteral num) {
            return num.getValue() == 0;
        } else if (node instanceof AsNumber num) {
            if (num.getOperand1() instanceof StringLiteral stringLiteral) {
                return stringLiteral.getText().isEmpty();
            }
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
