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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnnecessaryIfAfterUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardButtonAction;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardLaunch;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.LaunchButton;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * A script should execute actions when an event occurs. Instead of continuously checking for the event to occur
 * inside a forever or until loop it is only checked once in a conditional construct, making it
 * unlikely that the timing is correct.
 */
public class MissingLoopRobotSensing extends AbstractRobotFinder {
    public static final String NAME = "missing_loop_robot_sensing";
    private boolean insideLoop = false;
    private boolean inCondition = false;
    private boolean insideCompare = false;
    private boolean hasSensing = false;
    private boolean afterWaitUntil = false;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof BoardLaunch
                || node.getEvent() instanceof LaunchButton || node.getEvent() instanceof BoardButtonAction) {
            inCondition = false;
            super.visit(node);
            afterWaitUntil = false;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not be detected in Procedures
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(BoardButtonPressed node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(IRButtonPressed node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(LEDMatrixPosition node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(ObstaclesAhead node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(OrientateTo node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(PortOnLine node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(RobotButtonPressed node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(RobotShaken node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(RobotTilted node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(SeeColor node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(LessThan node) {
        if (inCondition && !afterWaitUntil) {
            insideCompare = true;
        }
        visitChildren(node);
        if (hasSensing) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
            hasSensing = false;
        }
        insideCompare = false;
    }

    @Override
    public void visit(BiggerThan node) {
        if (inCondition && !afterWaitUntil) {
            insideCompare = true;
        }
        visitChildren(node);
        if (hasSensing) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
            hasSensing = false;
        }
        insideCompare = false;
    }

    @Override
    public void visit(DetectDistancePort node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectAmbientLightPort node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectAmbientLight node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(AmbientLight node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectGrey node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectIRReflection node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectReflection node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(ShakingStrength node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(GyroPitchAngle node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(GyroRollAngle node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(DetectRGBValue node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(RotateXAngle node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(RotateYAngle node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(RotateZAngle node) {
        if (insideCompare) {
            hasSensing = true;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(WaitUntil node) {
        afterWaitUntil = true;
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public boolean areCoupled(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areCoupled(first, other);
        }

        if (other.getFinder() instanceof UnnecessaryIfAfterUntil) {
            return other.getFinder().areCoupled(other, first);
        }
        return false;
    }
}
