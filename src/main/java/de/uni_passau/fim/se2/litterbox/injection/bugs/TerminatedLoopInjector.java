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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TerminatedLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the {@link TerminatedLoop} bug by adding a stop block at the end of a loop.
 * <p>
 * Only injects if the loop doesn't already end with a terminating statement
 * (forever loop, stop block, or delete clone).
 */
public class TerminatedLoopInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new TerminatedLoop();
    }

    /**
     * Checks if the last statement in the list is already a terminating statement.
     */
    private boolean endsWithTerminatingStatement(StmtList stmtList) {
        if (stmtList.getStmts().isEmpty()) {
            return false;
        }
        Stmt lastStmt = stmtList.getStmts().getLast();
        return lastStmt instanceof RepeatForeverStmt
                || lastStmt instanceof StopThisScript
                || lastStmt instanceof StopAll
                || lastStmt instanceof DeleteClone;
    }

    private void inject(StmtList originalList) {
        // Skip empty loop bodies - adding a stop block makes no sense
        if (originalList.getStmts().isEmpty()) {
            return;
        }

        // Skip if already ends with a terminating statement
        if (endsWithTerminatingStatement(originalList)) {
            return;
        }

        addMutation(originalList, clonedList -> {
            StopThisScript stop = new StopThisScript(NonDataBlockMetadata.emptyNonBlockMetadata());
            clonedList.getStmts().add(stop);
            stop.setParentNode(clonedList);
        });
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        inject(node.getStmtList());
        super.visit(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        inject(node.getStmtList());
        super.visit(node);
    }

    @Override
    public void visit(UntilStmt node) {
        inject(node.getStmtList());
        super.visit(node);
    }
}
