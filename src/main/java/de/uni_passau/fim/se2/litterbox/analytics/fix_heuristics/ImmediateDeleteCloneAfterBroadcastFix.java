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

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

import java.util.List;

/**
 * Fix heuristic for the ImmediateDeleteCloneAfterBroadcast bug.
 *
 * <p>The bug is reported when Broadcast is immediately followed by DeleteClone.
 * The bug is fixed if the next statement is no longer DeleteClone.
 */
public class ImmediateDeleteCloneAfterBroadcastFix extends AbstractFixHeuristic {
    public static final String NAME = "immediate_delete_clone_after_broadcast_fix";

    public ImmediateDeleteCloneAfterBroadcastFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public void visit(Broadcast node) {
        if (matchesBugLocation(node)) {
            StmtList list = AstNodeUtil.findParent(node, StmtList.class);
            if (list != null) {
                List<Stmt> stmts = list.getStmts();
                int index = stmts.indexOf(node);
                if (index != -1 && index < stmts.size() - 1) {
                    Stmt nextStmt = stmts.get(index + 1);
                    if (!(nextStmt instanceof DeleteClone)) {
                        // Fixed: Next statement is NOT DeleteClone
                        addIssue(node, node.getMetadata());
                    }
                } else {
                    // Fixed: No next statement
                    addIssue(node, node.getMetadata());
                }
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        // If changed to BroadcastAndWait, that's also a fix
        if (matchesBugLocation(node)) {
            addIssue(node, node.getMetadata());
        }
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
