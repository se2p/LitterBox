/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;

/**
 * Fix heuristic for the MissingLoopMousePosition bug.
 *
 * <p>The bug is reported when mouse position is used outside a loop.
 * The bug is fixed if it's now inside a loop.
 */
public class MissingLoopMousePositionFix extends AbstractFixHeuristic {
    public static final String NAME = "missing_loop_mouse_position_fix";

    private boolean insideLoop;

    public MissingLoopMousePositionFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        super.visit(node);
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        super.visit(node);
        insideLoop = false;
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node) && insideLoop) {
            addIssue(node, node.getMetadata());
        }
    }

    private void checkMotion(SpriteMotionStmt node, Position pos) {
        if (matchesBugLocation(node)) {
            if (pos instanceof MousePos) {
                if (insideLoop) {
                    addIssue(node, node.getMetadata());
                }
            } else {
                // No longer MousePos - also fixed
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(PointTowards node) {
        checkMotion(node, node.getPosition());
        super.visit(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        checkMotion(node, node.getPosition());
        super.visit(node);
    }

    @Override
    public void visit(GoToPos node) {
        checkMotion(node, node.getPosition());
        super.visit(node);
    }

    @Override
    public void visit(MouseX node) {
        check(node);
    }

    @Override
    public void visit(MouseY node) {
        check(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
