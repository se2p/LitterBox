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

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveSides;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.MoveStop;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.RobotMoveStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

public class MotorOffScript extends AbstractRobotFinder {
    private static final String NAME = "motor_off_script";
    private boolean turnsOnMotor;
    private boolean turnsOffMotor;
    private boolean secondRun;

    @Override
    public void visit(ActorDefinition node) {
        turnsOnMotor = false;
        secondRun = false;
        super.visit(node);
        secondRun = true;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        turnsOffMotor = false;
        super.visit(node);
        if (secondRun && !node.getStmtList().getStmts().isEmpty()) {
            ASTNode last = node.getStmtList().getStmts().getLast();
            if (last instanceof StopAll || last instanceof StopOtherScriptsInSprite) {
                if (!(node.getEvent() instanceof Never) && turnsOnMotor && turnsOffMotor) {
                    addIssue(node.getEvent(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(MovingEmotion node) {
        turnsOnMotor = true;
    }

    @Override
    public void visit(RobotMoveStmt node) {
        if (!isZeroPower(node)) {
            turnsOnMotor = true;
        }
        if (secondRun && isZeroPower(node)) {
            turnsOffMotor = true;
        }
    }

    @Override
    public void visit(MoveStop node) {
        if (secondRun) {
            turnsOffMotor = true;
        }
    }

    private boolean isZeroPower(RobotMoveStmt node) {
        NumValueVisitor calc = new NumValueVisitor();
        try {
            if (node instanceof MoveDirection moveDirection) {
                return 0 == calc.calculateEndValue(moveDirection.getPercent());
            } else if (node instanceof MoveSides moveSides) {
                double left = calc.calculateEndValue(moveSides.getLeftPower());
                double right = calc.calculateEndValue(moveSides.getRightPower());
                return left == 0 && right == 0;
            }
        } catch (Exception ignored) {
        }
        return false;
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
