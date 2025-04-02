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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.SingularExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MissingLoopMousePosition extends AbstractIssueFinder {
    public static final String NAME = "missing_loop_mouse_position";
    private static final String MOTION = "missing_loop_mouse_position_motion";
    private static final String DIRECTION = "missing_loop_mouse_position_direction";
    private boolean insideLoop = false;
    private boolean inMotionStmtWithoutLoop = false;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            super.visit(node);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not be detected in Procedure
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
    public void visit(SetXTo node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(SetYTo node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(GoToPosXY node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        checkMotionStmt(node);
    }

    @Override
    public void visit(PointTowards node) {
        if (!insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = Hint.fromKey(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (!insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = Hint.fromKey(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(GoToPos node) {
        if (!insideLoop) {
            Position pos = node.getPosition();
            if (pos instanceof MousePos) {
                Hint hint = Hint.fromKey(DIRECTION);
                addIssue(node, node.getMetadata(), hint);
            }
        }
        visitChildren(node);
    }

    private void checkMotionStmt(SpriteMotionStmt node) {
        if (!insideLoop) {
            inMotionStmtWithoutLoop = true;
        }
        visitChildren(node);
        inMotionStmtWithoutLoop = false;
    }

    @Override
    public void visit(MouseX node) {
        checkMouse(node);
    }

    @Override
    public void visit(MouseY node) {
        checkMouse(node);
    }

    private void checkMouse(SingularExpression node) {
        if (inMotionStmtWithoutLoop) {
            Hint hint = Hint.fromKey(MOTION);
            addIssue(node, node.getMetadata(), hint);
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

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(DIRECTION);
        keys.add(MOTION);
        return keys;
    }
}
