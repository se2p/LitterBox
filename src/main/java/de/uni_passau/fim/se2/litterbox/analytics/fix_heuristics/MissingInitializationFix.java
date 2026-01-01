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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;


import java.util.Collections;
import java.util.Set;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;



/**
 * Fix heuristic for the MissingInitialization bug.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 *
 * <p>The bug is fixed if the variable use is now properly initialized before use.
 */
public class MissingInitializationFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new MissingInitialization();

    public MissingInitializationFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        // Find the target node
        BlockIdNodeFinder.find(program, getBugLocationBlockId()).ifPresent(context -> {
            if (!isBugStillPresent(context.node())) {
                // Fixed! The use is now properly initialized (or removed).
                currentActor = context.actor();
                currentScript = context.script();
                currentProcedure = context.procedure();
                addFix(context.node(), context.node().getMetadata());
            }
        });

        return Collections.unmodifiableSet(fixes);
    }
}
