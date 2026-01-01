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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.BlockingIfElse;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the BlockingIfElse bug by appending StopThisScript to both branches.
 *
 * <p>
 * Skips branches that already end with blocking statements (stop, delete clone, forever loop)
 * since appending a stop block would have no effect.
 */
public class BlockingIfElseInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new BlockingIfElse();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(IfElseStmt node) {
        // Determine if this if-else is in a context where blocking matters
        boolean hasSuccessors = hasSuccessorStatements(node);
        boolean inLoop = isInsideLoop(node);

        if (hasSuccessors || inLoop) {
            // Check if both branches are suitable for appending stop
            boolean thenSuitable = isSuitableForStopAppend(node.getThenStmts());
            boolean elseSuitable = isSuitableForStopAppend(node.getElseStmts());

            // Only inject if at least one branch can be modified
            if (thenSuitable || elseSuitable) {
                modifyClonedNode(node, clonedIfElse -> {
                    if (thenSuitable) {
                        StopThisScript stopThen = new StopThisScript(NonDataBlockMetadata.emptyNonBlockMetadata());
                        clonedIfElse.getThenStmts().getStmts().add(stopThen);
                        stopThen.setParentNode(clonedIfElse.getThenStmts());
                    }

                    if (elseSuitable) {
                        StopThisScript stopElse = new StopThisScript(NonDataBlockMetadata.emptyNonBlockMetadata());
                        clonedIfElse.getElseStmts().getStmts().add(stopElse);
                        stopElse.setParentNode(clonedIfElse.getElseStmts());
                    }
                });
            }
        }
        super.visit(node);
    }

    /**
     * Checks if a branch is suitable for appending a stop statement.
     * Returns false if the branch already ends with or contains a blocking statement.
     */
    protected boolean isSuitableForStopAppend(StmtList stmtList) {
        return !BlockingIfElse.isBlocking(stmtList.getStmts());
    }
}

