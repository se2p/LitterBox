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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;



/**
 * Fix heuristic for the RecursiveCloning bug.
 *
 * <p>The bug is reported when "When I start as clone" creates a clone of myself (infinite loop).
 * The bug is fixed if DeleteClone is present before the recursive clone creation.
 */
public class RecursiveCloningFix extends AbstractFixHeuristic {

    private boolean secondVisit;
    private boolean foundDelete;

    private final IssueFinder relatedBugFinder = new RecursiveCloning();

    public RecursiveCloningFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }




    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            foundDelete = false;
            secondVisit = false;

            // First pass: check if DeleteClone exists in the script
            visitChildren(node);
            
            // Second pass: if DeleteClone was found, report the fix at the buggy block location
            if (foundDelete) {
                secondVisit = true;
                visitChildren(node);
                secondVisit = false;
            }
            
            foundDelete = false; // Reset state
        }
    }

    @Override
    public void visit(Myself node) {
        if (secondVisit && matchesBugLocation(node.getParentNode())) {
            addFix(node.getParentNode(), node.getParentNode().getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(DeleteClone node) {
        if (!secondVisit) {
            foundDelete = true;
        }
        visitChildren(node);
    }
}
