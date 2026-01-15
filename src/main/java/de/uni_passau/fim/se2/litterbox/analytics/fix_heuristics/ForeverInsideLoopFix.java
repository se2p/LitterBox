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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the ForeverInsideLoop bug.
 *
 * <p>The bug is reported when a RepeatForever/Until/RepeatTimes loop is nested
 * inside another loop, causing the outer loop to never iterate.
 *
 * <p>The bug is fixed if:
 * <ul>
 *   <li>The loop is no longer nested inside another loop, OR</li>
 *   <li>The buggy loop was deleted (common fix)</li>
 * </ul>
 */
public class ForeverInsideLoopFix extends AbstractFixHeuristic {
    private boolean insideScript = false;
    private int loopDepth = 0;

    private final ForeverInsideLoop relatedBugFinder = new ForeverInsideLoop();

    public ForeverInsideLoopFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    protected void resetState() {
        insideScript = false;
        loopDepth = 0;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        boolean oldInside = insideScript;
        insideScript = true;
        visitChildren(node);
        insideScript = oldInside;
    }

    @Override
    public void visit(Script node) {
        boolean oldInside = insideScript;
        if (!(node.getEvent() instanceof Never)) {
            insideScript = true;
        }
        visitChildren(node);
        insideScript = oldInside;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        checkLoopFix(node);
        loopDepth++;
        visitChildren(node);
        loopDepth--;
    }

    @Override
    public void visit(UntilStmt node) {
        checkLoopFix(node);
        loopDepth++;
        visitChildren(node);
        loopDepth--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkLoopFix(node);
        loopDepth++;
        visitChildren(node);
        loopDepth--;
    }

    /**
     * Check if this loop is the target and is now fixed.
     * Fixed means: still in a script but no longer nested inside another loop.
     */
    private void checkLoopFix(LoopStmt node) {
        if (matchesBugLocation(node) && insideScript && loopDepth == 0) {
            // The loop exists, is inside a working script, and is NOT nested
            addFix(node, node.getMetadata());
        }
    }
}
