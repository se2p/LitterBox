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
import de.uni_passau.fim.se2.litterbox.analytics.NumValueVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.MovingEmotion;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState.*;

public class MotorStopScriptMissing extends AbstractRobotFinder {

    private static final String NAME = "motor_stop_script_missing";
    private final List<RobotMoveStmt> lastRunningList = new LinkedList<>();
    private RunningState state = NEVER;
    private RobotMoveStmt lastRunning = null;

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            script.accept(this);
            if (state == RUNNING) {
                lastRunningList.add(lastRunning);
            } else if (state == STOPPED) {
                state = NEVER;
                lastRunning = null;
                lastRunningList.clear();
                return;
            }
            state = NEVER;
            lastRunning = null;
        }
        for (RobotMoveStmt stmt : lastRunningList) {
            addIssue(stmt);
        }
        lastRunningList.clear();
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        proceduresInScript.put(node, new LinkedList<>());
        currentProcedure = null;
        if (!(node.getEvent() instanceof Never)) {
            node.getStmtList().accept(this);
        }
    }

    @Override
    public void visit(EmotionStmt node) {
        if (node instanceof MovingEmotion) {
            state = STOPPED;
        }
    }

    @Override
    public void visit(RobotMoveStmt node) {
        boolean zero = isZeroPower(node);
        if (zero || node instanceof TimedStmt || node instanceof TurnLeft2 || node instanceof TurnRight2 || node instanceof MoveStop) {
            state = STOPPED;
        } else {
            state = RUNNING;
            lastRunning = node;
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
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
