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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

/**
 * Fix heuristic for the StutteringMovement bug.
 *
 * <p>The bug is reported when movement blocks are used in response to key presses
 * outside of a loop, causing stuttering movement.
 *
 * <p>The bug is fixed if the movement is now inside a loop.
 */
public class StutteringMovementFix extends AbstractFixHeuristic {
    public static final String NAME = "stuttering_movement_fix";

    public StutteringMovementFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    private void checkIfFixed(ASTNode node) {
        if (matchesBugLocation(node)) {
            IfStmt ifStmt = AstNodeUtil.findParent(node, IfStmt.class);
            if (ifStmt != null && ifStmt.getBoolExpr() instanceof IsKeyPressed) {
                StmtList stmtList = AstNodeUtil.findParent(ifStmt, StmtList.class);
                if (stmtList == null) {
                    // StmtList not found - can't determine loop context
                    return;
                }

                if (
                        stmtList.getParentNode() instanceof UntilStmt
                                || stmtList.getParentNode() instanceof RepeatForeverStmt
                ) {
                    addIssue(node, node.getMetadata());
                }
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(MoveSteps node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(ChangeXBy node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(ChangeYBy node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(TurnRight node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(TurnLeft node) {
        checkIfFixed(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
