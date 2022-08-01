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

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDColorTimed;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.LEDColorTimedPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.FaceTimed;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.FaceTimedPort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.PlayFrequency;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * A sensing in a control structure can be interrupted if the control body has a stmt that takes a longer time like moving.
 */
public class InterruptedLoopSensingRobot extends AbstractRobotFinder {
    private static final String NAME = "interrupted_loop_sensing_robot";
    private boolean inCondition = false;
    private boolean insideEquals = false;
    private boolean sensingCollision = false;
    private boolean sensingOther = false;
    private boolean insideForever = false;
    private boolean insideControl = false;
    private boolean hasStop = false;
    private boolean hasStopInIf = false;
    private boolean checkingStop = false;
    private String blockName;
    private ASTNode variableName;
    private boolean checkingVariable;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!checkingVariable) {
            insideForever = true;
            visitChildren(node);
            insideForever = false;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (!checkingVariable) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            if (variableName != null) {
                checkForVariableChange(node.getStmtList());
            }
            inCondition = false;
            checkForStop(node.getStmtList());
            insideControl = true;
            blockName = IssueTranslator.getInstance().getInfo("until");
            node.getStmtList().accept(this);
            insideControl = false;
            sensingCollision = false;
            sensingOther = false;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!checkingVariable && !checkingStop) {
            if (insideForever) {
                inCondition = true;
                node.getBoolExpr().accept(this);
                if (variableName != null) {
                    checkForVariableChange(node.getThenStmts());
                    checkForVariableChange(node.getElseStmts());
                }
                inCondition = false;
                checkForStop(node.getThenStmts());
                checkForStop(node.getElseStmts());
                insideControl = true;
                blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then") + " " + IssueTranslator.getInstance().getInfo("else");
                node.getThenStmts().accept(this);
                node.getElseStmts().accept(this);
                insideControl = false;
                sensingCollision = false;
                sensingOther = false;
            }
        } else if (!hasStop && checkingStop) {
            node.getThenStmts().accept(this);
            if (hasStop) {
                hasStop = false;
                hasStopInIf = true;
            }
            if (hasStopInIf) {
                node.getElseStmts().accept(this);
                hasStopInIf = false;
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!checkingVariable && !checkingStop && insideForever) {
            inCondition = true;
            node.getBoolExpr().accept(this);
            if (variableName != null) {
                checkForVariableChange(node.getThenStmts());
            }
            inCondition = false;
            checkForStop(node.getThenStmts());
            insideControl = true;
            blockName = IssueTranslator.getInstance().getInfo("if") + " " + IssueTranslator.getInstance().getInfo("then");
            node.getThenStmts().accept(this);
            insideControl = false;
            sensingCollision = false;
            sensingOther = false;
        }
    }

    /**
     * If the variable is changed inside these stmts, it should not trigger the finder, as the insides of the loop are responsible for the exit condition.
     *
     * @param stmts stmts that should be searched
     */
    private void checkForVariableChange(StmtList stmts) {
        checkingVariable = true;
        stmts.accept(this);
        checkingVariable = false;
        variableName = null;
    }

    /**
     * If the stmts only lead to a stop without alternate control flow the interruption should not be detected.
     *
     * @param stmts stmts that should be searched
     */
    private void checkForStop(StmtList stmts) {
        checkingStop = true;
        stmts.accept(this);
        checkingStop = false;
        if (hasStop) {
            sensingCollision = false;
            sensingOther = false;
        }
        hasStop = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(AddTo node) {
        if (checkingVariable) {
            if (node.getIdentifier().equals(variableName)) {
                sensingOther = false;
            }
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(KeepBackwardTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(KeepForwardTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(MoveForwardTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(MoveBackwardTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(TurnLeftTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(TurnRightTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && (sensingCollision || sensingOther)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(FaceTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(FaceTimedPort node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(LEDColorTimed node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(LEDColorTimedPosition node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(PlayFrequency node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("think_seconds")); //TODO
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (!checkingVariable && !checkingStop) {
            if (insideControl && sensingOther) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.THEN_ELSE, blockName);
                hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds"));
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(BoardButtonPressed node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingOther = true;
        }
    }

    @Override
    public void visit(RobotButtonPressed node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingOther = true;
        }
    }

    @Override
    public void visit(RobotShaken node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(RobotTilted node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(OrientateTo node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(ObstaclesAhead node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(IRButtonPressed node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingOther = true;
        }
    }

    @Override
    public void visit(SeeColor node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(PortOnLine node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(DetectDistancePort node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(LEDMatrixPosition node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = false;
        }
    }

    @Override
    public void visit(DetectLinePort node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(ShakingStrength node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(DetectRGBValue node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(DetectAmbientLight node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(DetectAmbientLightPort node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(AmbientLight node) {
        if (!checkingVariable && !checkingStop && inCondition) {
            sensingCollision = true;
        }
    }

    @Override
    public void visit(Equals node) {
        if (!checkingVariable && !checkingStop) {
            if (inCondition) {
                insideEquals = true;
            }
            visitChildren(node);
            insideEquals = false;
        }
    }

    @Override
    public void visit(Variable node) {
        if (!checkingVariable && !checkingStop && insideEquals) {
            sensingOther = true;
            variableName = node.getParentNode();
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (!checkingVariable && !checkingStop && insideEquals) {
            sensingOther = true;
            variableName = node.getIdentifier();
        }
    }

    @Override
    public void visit(StopAll node) {
        if (checkingStop) {
            hasStop = true;
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (checkingStop) {
            hasStop = true;
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
