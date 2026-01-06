/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopMousePosition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the MissingLoopMousePosition bug.
 *
 * <p>The bug is reported when mouse position is used outside a loop.
 * The bug is fixed if it's now inside a loop.
 */
public class MissingLoopMousePositionFix extends AbstractFixHeuristic {

    private boolean insideLoop;

    private final MissingLoopMousePosition relatedBugFinder = new MissingLoopMousePosition();

    public MissingLoopMousePositionFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        insideLoop = false;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        boolean wasInsideLoop = insideLoop;
        insideLoop = true;
        visitChildren(node);
        insideLoop = wasInsideLoop;
    }

    @Override
    public void visit(UntilStmt node) {
        boolean wasInsideLoop = insideLoop;
        insideLoop = true;
        visitChildren(node);
        insideLoop = wasInsideLoop;
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node) && insideLoop) {
            addFix(node, node.getMetadata());
        }
    }

    private void checkMotion(SpriteMotionStmt node, Position pos) {
        if (matchesBugLocation(node)) {
            if (pos instanceof MousePos) {
                if (insideLoop) {
                    addFix(node, node.getMetadata());
                }
            } else {
                // No longer MousePos - also fixed
                addFix(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(PointTowards node) {
        checkMotion(node, node.getPosition());
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        checkMotion(node, node.getPosition());
        visitChildren(node);
    }

    @Override
    public void visit(GoToPos node) {
        checkMotion(node, node.getPosition());
        visitChildren(node);
    }

    @Override
    public void visit(MouseX node) {
        check(node);
    }

    @Override
    public void visit(MouseY node) {
        check(node);
    }
}
