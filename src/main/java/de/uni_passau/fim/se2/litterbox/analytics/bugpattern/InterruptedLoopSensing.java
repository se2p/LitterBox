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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * A sensing in a control structure can be interrupted if the control body has a stmt that takes a longer time like
 * gliding.
 */
public class InterruptedLoopSensing extends AbstractIssueFinder {
    private static final String NAME = "interrupted_loop_sensing";
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
    private boolean visitOuter;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!checkingVariable && !visitOuter) {
            insideForever = true;
            visitChildren(node);
            insideForever = false;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (!checkingVariable && !visitOuter) {
            blockName = IssueTranslator.getInstance().getInfo("until");
            inCondition = true;
            node.getBoolExpr().accept(this);
            if (variableName != null) {
                checkForVariableChange(node.getStmtList());
            }
            inCondition = false;
            checkForStop(node.getStmtList());
            insideControl = true;
            node.getStmtList().accept(this);
            insideControl = false;
            sensingCollision = false;
            sensingOther = false;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!checkingVariable && !checkingStop && !visitOuter) {
            if (insideForever) {
                blockName = IssueTranslator.getInstance().getInfo("if") + " "
                        + IssueTranslator.getInstance().getInfo("then") + " "
                        + IssueTranslator.getInstance().getInfo("else");
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
                node.getThenStmts().accept(this);
                node.getElseStmts().accept(this);
                insideControl = false;
                sensingCollision = false;
                sensingOther = false;
            }
        } else if (!hasStop && checkingStop && !visitOuter) {
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
        if (!checkingVariable && !checkingStop && insideForever && !visitOuter) {
            blockName = IssueTranslator.getInstance().getInfo("if") + " "
                    + IssueTranslator.getInstance().getInfo("then");
            inCondition = true;
            node.getBoolExpr().accept(this);
            if (variableName != null) {
                checkForVariableChange(node.getThenStmts());
            }
            inCondition = false;
            checkForStop(node.getThenStmts());
            insideControl = true;
            node.getThenStmts().accept(this);
            insideControl = false;
            sensingCollision = false;
            sensingOther = false;
        }
    }

    /**
     * If the variable is changed inside these stmts, it should not trigger the finder, as the insides of the loop are
     * responsible for the exit condition.
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
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
        }
    }

    @Override
    public void visit(AddTo node) {
        if (checkingVariable && node.getIdentifier().equals(variableName)) {
            sensingOther = false;
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
    public void visit(GlideSecsTo node) {
        if (!checkingVariable && !checkingStop && (insideControl && (sensingCollision || sensingOther))) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (!checkingVariable && !checkingStop && (insideControl && (sensingCollision || sensingOther))) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("glide_secs_to_xy"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (!checkingVariable && !checkingStop && (insideControl && sensingOther)) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("wait_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (!checkingVariable && !checkingStop && (insideControl && sensingOther)) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("think_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("think_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (!checkingVariable && !checkingStop && (insideControl && sensingOther)) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("play_sound_until_done"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("play_sound_until_done"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (!checkingVariable && !checkingStop && (insideControl && sensingOther)) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("say_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
        if (visitOuter) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.THEN_ELSE, blockName);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("say_seconds"));
            addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingOther = true;
            visitOuterBlocks(node);
        }
    }

    private void visitOuterBlocks(ASTNode node) {
        visitOuter = true;
        StmtList stmtList = AstNodeUtil.findParent(node.getParentNode(), StmtList.class);
        // if the outer StmtList is the top StmtList of the Script, checking is not necessary
        if (!(stmtList.getParentNode() instanceof ScriptEntity)) {
            stmtList.accept(this);
        }
        visitOuter = false;
    }

    @Override
    public void visit(Touching node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingCollision = true;
            visitOuterBlocks(node);
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingOther = true;
            visitOuterBlocks(node);
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingCollision = true;
            visitOuterBlocks(node);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingCollision = true;
            visitOuterBlocks(node);
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (!checkingVariable && !checkingStop && inCondition && !visitOuter) {
            sensingCollision = true;
            visitOuterBlocks(node);
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
        if (!checkingVariable && !checkingStop && insideEquals && !visitOuter) {
            sensingOther = true;
            variableName = node.getParentNode();
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (!checkingVariable && !checkingStop && insideEquals && !visitOuter) {
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
    public void visit(WaitUntil node) {
        if (!visitOuter && insideForever) {
            blockName = IssueTranslator.getInstance().getInfo("wait_until");
            inCondition = true;
            visitChildren(node);
            inCondition = false;
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
