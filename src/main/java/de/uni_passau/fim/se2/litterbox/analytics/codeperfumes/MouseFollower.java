/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

/**
 * The sprite follows the mouse in a RepeatForever block.
 */
public class MouseFollower extends AbstractIssueFinder {

    public static final String NAME = "mouse_follower";
    private boolean insideLoop = false;
    private boolean pointToMouse = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        iterateStmts(node.getStmtList());
        insideLoop = false;
    }

    @Override
    public void visit(GoToPos node) {
        if (insideLoop && node.getPosition() instanceof MousePos) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PointTowards node) {
        if (insideLoop && node.getPosition() instanceof MousePos) {
            pointToMouse = true;
        }
        visitChildren(node);
    }

    @Override
    public void visit(MoveSteps node) {
        if (insideLoop && pointToMouse) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            pointToMouse = false;
        }
        visitChildren(node);
    }

    private void iterateStmts(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof GoToPos || stmt instanceof MoveSteps) {
                stmt.accept(this);
                pointToMouse = false;
            } else if (stmt instanceof PointTowards) {
                stmt.accept(this);
            } else {
                pointToMouse = false;
            }
        });
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
