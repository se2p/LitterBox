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
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;

import java.util.List;

/**
 * The sprite follows another object (another sprite).
 */
public class ObjectFollower extends AbstractIssueFinder {

    public static final String NAME = "object_follower";
    private boolean insideLoop = false;
    private boolean pointToObject = false;

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
        if (insideLoop && node.getPosition() instanceof FromExpression) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
    }

    @Override
    public void visit(PointTowards node) {

        // Check if it is another sprite (not MousePos or RandomPos)
        if (insideLoop && node.getPosition() instanceof FromExpression) {
            pointToObject = true;
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (insideLoop && pointToObject) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            pointToObject = false;
        }
    }

    private void iterateStmts(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof GoToPos || stmt instanceof MoveSteps) {
                stmt.accept(this);
                pointToObject = false;
            } else if (stmt instanceof PointTowards) {
                stmt.accept(this);
            } else {
                pointToObject = false;
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
