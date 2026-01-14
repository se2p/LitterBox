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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the MissingEraseAll bug by removing ALL PenClearStmt (erase all) blocks.
 *
 * <p>
 * Precondition: The program must have a PenDown statement for the bug to be meaningful.
 */
public class MissingEraseAllInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MissingEraseAll();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Precondition: Must have PenDown
        if (!hasPenDown(node)) {
            return;
        }

        // Collect all StmtLists that contain PenClearStmt
        List<StmtList> stmtListsWithPenClear = new ArrayList<>();
        collectStmtListsWithPenClear(node, stmtListsWithPenClear);
        if (stmtListsWithPenClear.isEmpty()) {
            return;
        }

        // Create ONE mutation that removes ALL erase all blocks from their StmtLists
        modifyMultipleClonedNodes(stmtListsWithPenClear, (StmtList clonedList) ->
                clonedList.getStmts().removeIf(stmt -> stmt instanceof PenClearStmt));
    }

    private boolean hasPenDown(ASTNode node) {
        if (node instanceof PenDownStmt) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (hasPenDown(child)) {
                return true;
            }
        }
        return false;
    }

    private void collectStmtListsWithPenClear(ASTNode node, List<StmtList> result) {
        if (node instanceof StmtList stmtList) {
            for (Stmt stmt : stmtList.getStmts()) {
                if (stmt instanceof PenClearStmt) {
                    result.add(stmtList);
                    break; // Only add StmtList once even if multiple PenClear
                }
            }
        }
        for (ASTNode child : node.getChildren()) {
            collectStmtListsWithPenClear(child, result);
        }
    }
}
