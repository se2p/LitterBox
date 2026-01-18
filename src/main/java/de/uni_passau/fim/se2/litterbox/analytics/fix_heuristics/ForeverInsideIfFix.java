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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the ForeverInsideIf bug.
 *
 * <p>The bug is reported when a RepeatForever is inside an If with following statements.
 * The bug is fixed if:
 * <ul>
 *   <li>The forever block is no longer inside an If, OR</li>
 *   <li>No statements follow the If, OR</li>
 *   <li>The forever block was deleted (common fix)</li>
 * </ul>
 */
public class ForeverInsideIfFix extends AbstractFixHeuristic {

    private boolean insideIf;
    private int ifFollowingCounter;

    private final ForeverInsideIf relatedBugFinder = new ForeverInsideIf();

    public ForeverInsideIfFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        insideIf = false;
        ifFollowingCounter = 0;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(StmtList node) {
        int oldCounter = ifFollowingCounter;
        boolean hasIf = false;
        // Check if there's an If with following statements
        for (int i = 0; i < node.getStmts().size() - 1; i++) {
            if (node.getStmts().get(i) instanceof IfStmt) {
                ifFollowingCounter++;
                hasIf = true;
                break;
            }
        }
        visitChildren(node);
        if (hasIf) {
            ifFollowingCounter = oldCounter;
        }
    }

    @Override
    public void visit(IfStmt node) {
        boolean wasInsideIf = insideIf;
        insideIf = true;
        visitChildren(node);
        insideIf = wasInsideIf;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (matchesBugLocation(node)) {
            // Fixed if not inside If OR no following statements
            if (!insideIf || ifFollowingCounter <= 0) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}

