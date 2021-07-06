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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;

import java.util.List;

/**
 * Collision detects if the Sprite Look or Sprite Motion changes after a touch event occurs. There must be a IfThenStmt
 * in a RepeatForeverStmt to continuously check for the the touch event.
 */
public class Collision extends AbstractIssueFinder {
    public static final String NAME = "collision";
    private boolean inRepeatForever = false;
    private boolean changeAfterTouching = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        inRepeatForever = true;
        visitChildren(node);
        inRepeatForever = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        changeAfterTouching = false;
        if (inRepeatForever) {

            // Checks if the condition is of type touching (e.g. touched the edge?)
            if (node.getBoolExpr() instanceof Touching) {
                List<Stmt> stmts = node.getThenStmts().getStmts();

                // Checks if after touching event any look or motion stmt exists
                changeAfterTouching = stmts.stream().anyMatch(stmt ->
                        stmt instanceof SpriteLookStmt || stmt instanceof SpriteMotionStmt);

                if (changeAfterTouching) {
                    addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
                }
            } else {
                visitChildren(node);
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
