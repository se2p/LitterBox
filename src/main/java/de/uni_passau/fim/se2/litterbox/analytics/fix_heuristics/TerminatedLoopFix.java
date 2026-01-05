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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TerminatedLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.List;

/**
 * Fix heuristic for the TerminatedLoop bug.
 *
 * <p>The bug is reported on loop nodes (forever, repeat, until) when they have an
 * unconditional StopAll or StopThisScript as the last statement.
 *
 * <p>The bug is considered fixed if the loop no longer ends with a stop block.
 */
public class TerminatedLoopFix extends AbstractFixHeuristic {

    private final TerminatedLoop relatedBugFinder = new TerminatedLoop();

    public TerminatedLoopFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        checkLoopNode(node, node.getStmtList());
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkLoopNode(node, node.getStmtList());
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        checkLoopNode(node, node.getStmtList());
        visitChildren(node);
    }

    /**
     * Checks if a loop node matches the bug location and if the bug is fixed.
     * The bug is fixed if the loop no longer ends with a stop block.
     */
    private void checkLoopNode(Stmt loopNode, StmtList children) {
        if (!matchesBugLocation(loopNode)) {
            return;
        }

        // Found the loop that matches the bug location
        // Check if it's still buggy (ends with stop block)
        if (!children.hasStatements()) {
            // Empty loop - fixed (no stop block)
            addFix(loopNode, loopNode.getMetadata());
            return;
        }

        List<Stmt> stmts = children.getStmts();
        Stmt last = stmts.getLast();

        if (!(last instanceof StopAll) && !(last instanceof StopThisScript)) {
            // Last statement is not a stop block - fixed!
            addFix(loopNode, loopNode.getMetadata());
        }
        // Otherwise, still buggy - don't report a fix
    }
}

