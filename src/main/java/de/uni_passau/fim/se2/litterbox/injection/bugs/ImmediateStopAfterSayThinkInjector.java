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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSayThink;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injects ImmediateStopAfterSay bug by inserting StopAll after Say/Think statements.
 *
 * <p>
 * The bug is: Say/Think immediately followed by StopAll as the last two statements.
 * StopAll cannot have successors, so statements after it are removed.
 *
 * <p>
 * Strategy: Visit StmtList nodes, find async Say/Think statements, and inject StopAll after them.
 * If no async versions exist, convert SayForSecs/ThinkForSecs at the end of statement lists.
 *
 * <p>
 * Note: This implementation uses index-based access to avoid issues with indexOf() when
 * multiple structurally-equal statements exist (since equals() compares by structure).
 */
public class ImmediateStopAfterSayThinkInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ImmediateStopAfterSayThink();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> statements = node.getStmts();

        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);

            // Check for async Say/Think not already followed by StopAll
            if (stmt instanceof Say || stmt instanceof Think) {
                if (!isFollowedByStopAll(statements, i)) {
                    injectStopAfterAtIndex(node, i);
                }
            } else if (i == statements.size() - 1) {
                if (stmt instanceof SayForSecs) {
                    convertTimedToAsyncWithStop(node, i, true);
                } else if (stmt instanceof ThinkForSecs) {
                    convertTimedToAsyncWithStop(node, i, false);
                }
            }
        }

        super.visit(node);
    }

    /**
     * Injects StopAll after the statement at the given index.
     */
    private void injectStopAfterAtIndex(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
            List<Stmt> stmts = clonedList.getStmts();
            if (index < stmts.size()) {
                // Remove all statements after Say/Think (StopAll can't have successors)
                while (stmts.size() > index + 1) {
                    stmts.remove(index + 1);
                }

                // Insert StopAll after Say/Think
                StopAll stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
                stmts.add(stopAll);
                stopAll.setParentNode(clonedList);
            }
        });
    }

    /**
     * Converts a timed Say/Think statement to its async version and adds StopAll.
     */
    private void convertTimedToAsyncWithStop(StmtList node, int index, boolean isSay) {
        modifyClonedNode(node, clonedList -> {
            List<Stmt> stmts = clonedList.getStmts();
            if (index < stmts.size()) {
                // Create async version
                Stmt asyncStmt;
                if (isSay) {
                    SayForSecs sayForSecs = (SayForSecs) stmts.get(index);
                    asyncStmt = new Say(sayForSecs.getString(), sayForSecs.getMetadata());
                } else {
                    ThinkForSecs thinkForSecs = (ThinkForSecs) stmts.get(index);
                    asyncStmt = new Think(thinkForSecs.getThought(), thinkForSecs.getMetadata());
                }

                // Replace timed with async and add StopAll
                stmts.set(index, asyncStmt);
                asyncStmt.setParentNode(clonedList);

                StopAll stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
                stmts.add(stopAll);
                stopAll.setParentNode(clonedList);
            }
        });
    }

    /**
     * Checks if the statement at the given index is followed by StopAll.
     */
    private boolean isFollowedByStopAll(List<Stmt> stmts, int index) {
        return index + 1 < stmts.size() && stmts.get(index + 1) instanceof StopAll;
    }
}

