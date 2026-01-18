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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousParameterNameUsed;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the AmbiguousParameterNameUsed bug.
 *
 * <p>The bug is reported when a procedure has multiple parameters with the same name.
 * The bug is fixed if all parameter names are now unique.
 */
public class AmbiguousParameterNameUsedFix extends AbstractFixHeuristic {

    private final AmbiguousParameterNameUsed relatedBugFinder = new AmbiguousParameterNameUsed();

    public AmbiguousParameterNameUsedFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        visitChildren(node);
        if (matchesBugLocation(node)) {
            boolean stillPresent = isBugStillPresent(node);

            if (!stillPresent) {
                addFix(node, node.getMetadata());
            }
        }
    }
}
