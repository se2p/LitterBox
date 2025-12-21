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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSay;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injects ImmediateStopAfterSay bug by inserting StopAll after Say/Think statements.
 * <p>
 * The bug is: Say/Think immediately followed by StopAll as the last two statements.
 * StopAll cannot have successors, so statements after it are removed.
 */
public class ImmediateStopAfterSayInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ImmediateStopAfterSay();
    }

    @Override
    public void visit(Say node) {
        injectStopAfter(node);
        super.visit(node);
    }

    @Override
    public void visit(Think node) {
        injectStopAfter(node);
        super.visit(node);
    }

    private void injectStopAfter(Stmt sayOrThink) {
        // Skip if already followed by StopAll
        if (isFollowedByStopAll(sayOrThink)) {
            return;
        }

        addMutation(sayOrThink, clonedStmt -> {
            ASTNode parent = clonedStmt.getParentNode();
            if (parent instanceof StmtList stmtList) {
                List<Stmt> stmts = stmtList.getStmts();
                int index = stmts.indexOf(clonedStmt);
                if (index >= 0) {
                    // Remove all statements after Say/Think (StopAll can't have successors)
                    while (stmts.size() > index + 1) {
                        stmts.remove(index + 1);
                    }

                    // Insert StopAll after Say/Think
                    StopAll stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
                    stmts.add(stopAll);
                    stopAll.setParentNode(stmtList);
                }
            }
        });
    }

    private boolean isFollowedByStopAll(Stmt stmt) {
        ASTNode parent = stmt.getParentNode();
        if (parent instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(stmt);
            if (index >= 0 && index + 1 < stmts.size()) {
                return stmts.get(index + 1) instanceof StopAll;
            }
        }
        return false;
    }
}
