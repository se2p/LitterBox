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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;

/**
 * Directed Motion means after a key press the sprite points to a certain direction and moves a number of steps. That
 * means within a key press script there has to be a PointToDirection and a MoveSteps block in that order.
 */
public class DirectedMotion extends AbstractIssueFinder {

    public static final String NAME = "directed_motion";
    private boolean pointInDirection = false;
    private boolean keyPressed = false;

    @Override
    public void visit(Script node) {
        keyPressed = false;
        pointInDirection = false;
        if (node.getEvent() instanceof KeyPressed) {
            keyPressed = true;
            super.visit(node);
            keyPressed = false;
            pointInDirection = false;
        }
        visitChildren(node);
    }

    @Override
    public void visit(StmtList node) {
        if (keyPressed) {
            for (Stmt stmt : node.getStmts()) {
                if (stmt instanceof PointInDirection) {
                    pointInDirection = true;
                } else if (stmt instanceof MoveSteps) {
                    if (pointInDirection) {
                        addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                        break;
                    }
                }
            }
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
