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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.getKeyValue;

/**
 * A common way to move sprites in response to keyboard input is to use the specific event handler When key
 * pressed followed by a move steps, change x by or change y by statement.
 * Compared to the alternative to use a forever loop with a conditional containing a key pressed?
 * expression, the first approach results in noticeably slower reaction and stuttering movement of the sprite moved.
 */
public class StutteringMovement extends AbstractIssueFinder {

    public static final String NAME = "stuttering_movement";
    private boolean hasPositionMove;
    private boolean hasRotation;
    private int loopCount = 0;

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        loopCount = 0;
        currentScript = script;
        currentProcedure = null;
        visitChildren(script);
        if (script.getEvent() instanceof KeyPressed) {
            List<Stmt> listOfStmt = script.getStmtList().getStmts();
            if (listOfStmt.size() <= 2 && !listOfStmt.isEmpty()) {
                Stmt stmt = listOfStmt.get(0);
                if (hasRotation || hasPositionMove) {
                    KeyPressed keyPressed = (KeyPressed) script.getEvent();
                    String key = getKeyValue((int) ((NumberLiteral) keyPressed.getKey().getKey()).getValue());
                    Hint hint = new Hint(getName());
                    hint.setParameter(Hint.HINT_KEY, key);
                    addIssue(stmt, stmt.getMetadata(), IssueSeverity.HIGH, hint);
                }
            }
        }
        hasPositionMove = false;
        hasRotation = false;
        currentScript = null;
    }

    @Override
    public void visit(MoveSteps node) {
        if (loopCount == 0) {
            hasPositionMove = true;
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (loopCount == 0) {
            hasPositionMove = true;
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (loopCount == 0) {
            hasPositionMove = true;
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (loopCount == 0) {
            hasRotation = true;
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (loopCount == 0) {
            hasRotation = true;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }

        ASTNode firstNode = first.getCodeLocation();
        ASTNode secondNode = other.getCodeLocation();

        if ((firstNode instanceof TurnLeft || firstNode instanceof TurnRight) && (secondNode instanceof TurnLeft || secondNode instanceof TurnRight)) {
            return true;
        }
        if (firstNode instanceof MoveSteps && secondNode instanceof MoveSteps) {
            return true;
        }
        return first.getCodeLocation().equals(other.getCodeLocation());
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
