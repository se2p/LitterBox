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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

/**
 * To avoid slower and stuttering movements it is recommended to use a forever loop with a conditional containing a
 * key pressed? expression, followed by a move steps, change x by or change y by statement (or a list of them). This
 * is the solution for the "Stuttering Movement" bug.
 */
public class MovementInLoop extends AbstractIssueFinder {

    public static final String NAME = "movement_in_loop";
    private boolean hasKeyPressed;
    private boolean insideLoop;
    private boolean inCondition;
    private boolean subsequentMovement;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }

        subsequentMovement = false;
        inCondition = false;
        insideLoop = false;
        hasKeyPressed = false;
        super.visit(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(MoveSteps node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(PointInDirection node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (hasKeyPressed && !subsequentMovement) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            subsequentMovement = true;
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (insideLoop && inCondition) {
            hasKeyPressed = true;
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
