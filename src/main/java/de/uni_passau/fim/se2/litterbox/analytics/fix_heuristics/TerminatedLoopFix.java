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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TerminatedLoop;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.List;

/**
 * Fix heuristic for the TerminatedLoop bug.
 *
 * <p>The bug is reported when a loop has an unconditional StopAll or StopThisScript
 * as its last statement, causing the loop to execute only once.
 *
 * <p>The bug is considered fixed if:
 * <ul>
 *   <li>The stop block is no longer inside a loop</li>
 *   <li>The stop block is no longer the last statement in the loop</li>
 *   <li>The stop block was removed or replaced</li>
 * </ul>
 */
public class TerminatedLoopFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new TerminatedLoop();

    public TerminatedLoopFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(StopAll node) {
        checkStopBlock(node);
        super.visit(node);
    }

    @Override
    public void visit(StopThisScript node) {
        checkStopBlock(node);
        super.visit(node);
    }

    private void checkStopBlock(Stmt node) {
        if (!matchesBugLocation(node)) {
            return;
        }

        // The block exists. Check if it's still buggy (last in a loop).
        ASTNode parent = node.getParentNode();
        if (!(parent instanceof StmtList stmtList)) {
            // Not in a statement list context - fixed!
            addIssue(node, node.getMetadata());
            return;
        }

        ASTNode grandParent = parent.getParentNode();
        boolean inLoop = grandParent instanceof RepeatForeverStmt
                || grandParent instanceof RepeatTimesStmt
                || grandParent instanceof UntilStmt;

        if (!inLoop) {
            // Not in a loop - fixed!
            addIssue(node, node.getMetadata());
            return;
        }

        // Check if still the last statement
        List<Stmt> stmts = stmtList.getStmts();
        if (stmts.getLast() != node) {
            // No longer the last statement - fixed!
            addIssue(node, node.getMetadata());
        }
        // Otherwise, still buggy - don't report a fix
    }
}
