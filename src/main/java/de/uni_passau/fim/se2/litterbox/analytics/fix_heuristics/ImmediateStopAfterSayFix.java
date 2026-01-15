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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSayThink;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.List;

/**
 * Fix heuristic for the ImmediateStopAfterSay bug.
 *
 * <p>The bug is reported when Say/Think is immediately followed by StopAll.
 * The bug is fixed if the next statement is no longer StopAll.
 */
public class ImmediateStopAfterSayFix extends AbstractFixHeuristic {

    private final ImmediateStopAfterSayThink relatedBugFinder = new ImmediateStopAfterSayThink();

    public ImmediateStopAfterSayFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(Say node) {
        checkFixed(node);
        visitChildren(node);
    }

    @Override
    public void visit(SayForSecs node) {
        // If the bug location points to a SayForSecs, it means the Say block
        // was converted to SayForSecs, which is a valid fix (it waits).
        // So we can immediately mark it as fixed.
        if (matchesBugLocation(node)) {
            addFix(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Think node) {
        checkFixed(node);
        visitChildren(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        // Same logic as SayForSecs
        if (matchesBugLocation(node)) {
            addFix(node, node.getMetadata());
        }
        visitChildren(node);
    }

    private void checkFixed(Stmt node) {
        if (matchesBugLocation(node)) {
            StmtList list = AstNodeUtil.findParent(node, StmtList.class);
            if (list != null) {
                List<Stmt> stmts = list.getStmts();
                int index = stmts.indexOf(node);
                if (index != -1 && index < stmts.size() - 1) {
                    Stmt nextStmt = stmts.get(index + 1);
                    if (!(nextStmt instanceof StopAll)) {
                        // Fixed: Next statement is NOT StopAll
                        addFix(node, node.getMetadata());
                    }
                } else {
                    // Fixed: No next statement
                    addFix(node, node.getMetadata());
                }
            }
        }
    }
}
