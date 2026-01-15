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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InappropriateHandlerDeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the InappropriateHandlerDeleteClone bug.
 *
 * <p>The bug is reported when DeleteClone is used inside a GreenFlag script
 * (which runs on the main sprite, not a clone).
 * The bug is fixed if DeleteClone is no longer inside a GreenFlag script.
 */
public class InappropriateHandlerDeleteCloneFix extends AbstractFixHeuristic {

    private boolean insideGreenFlag;

    private final InappropriateHandlerDeleteClone relatedBugFinder = new InappropriateHandlerDeleteClone();

    public InappropriateHandlerDeleteCloneFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        insideGreenFlag = false;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(Script node) {
        boolean wasInside = insideGreenFlag;
        if (node.getEvent() instanceof GreenFlag) {
            insideGreenFlag = true;
        }
        visitChildren(node);
        insideGreenFlag = wasInside;
    }

    @Override
    public void visit(DeleteClone node) {
        if (matchesBugLocation(node)) {
            // Fixed if NOT in a GreenFlag script
            if (!insideGreenFlag) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}
