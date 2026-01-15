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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the EndlessRecursion bug.
 *
 * <p>The bug is reported when a procedure calls itself without any conditional guard.
 * The bug is fixed if the recursive call is now guarded by a conditional.
 */
public class EndlessRecursionFix extends AbstractFixHeuristic {

    private int conditionalNestingDepth;
    private String currentProcedureName;
    private boolean insideProcedure;

    private final EndlessRecursion relatedBugFinder = new EndlessRecursion();

    public EndlessRecursionFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    protected void resetState() {
        conditionalNestingDepth = 0;
        currentProcedureName = null;
        insideProcedure = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        if (procMap != null && procMap.containsKey(node.getIdent())) {
            currentProcedureName = procMap.get(node.getIdent()).getName();
        }
        visitChildren(node);
        insideProcedure = false;
        currentProcedureName = null;
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node)) {
            // Check if recursive (calls same procedure)
            boolean isRecursive = insideProcedure
                    && currentProcedureName != null
                    && currentProcedureName.equals(node.getIdent().getName());

            if (isRecursive) {
                // Fixed if guarded by a conditional (conditionalNestingDepth > 0)
                if (conditionalNestingDepth > 0) {
                    addFix(node, node.getMetadata());
                }
            } else {
                // No longer recursive - fixed!
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        conditionalNestingDepth++;
        visitChildren(node);
        conditionalNestingDepth--;
    }

    @Override
    public void visit(IfThenStmt node) {
        conditionalNestingDepth++;
        visitChildren(node);
        conditionalNestingDepth--;
    }
}
