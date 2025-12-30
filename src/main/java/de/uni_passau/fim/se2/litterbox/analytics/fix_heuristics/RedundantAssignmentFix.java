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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Set;

/**
 * Fix heuristic for the RedundantAssignment bug.
 *
 * <p>The bug is reported when a variable or attribute is assigned a value
 * that is immediately overwritten without being used.
 *
 * <p>The bug is fixed if the assignment is no longer redundant (i.e., it is
 * now used before being overwritten, or the overwriting assignment was removed).
 */
public class RedundantAssignmentFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new RedundantAssignment();

    public RedundantAssignmentFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public Set<Issue> check(Program program) {
        this.program = program;

        // Use composition: create a new bug finder instance
        RedundantAssignment bugFinder = new RedundantAssignment();
        Set<Issue> foundBugs = bugFinder.check(program);

        // Find the target node using utility
        BlockIdNodeFinder.find(program, bugLocationBlockId).ifPresent(context -> {
            // Check if the bug still exists at this location
            boolean bugPersists = foundBugs.stream()
                    .anyMatch(issue -> issue.getCodeLocation() == context.node());

            if (!bugPersists) {
                // Fixed! The block exists but is no longer redundant.
                IssueBuilder builder = new IssueBuilder()
                        .withFinder(this)
                        .withCurrentNode(context.node())
                        .withActor(context.actor())
                        .withProgram(program)
                        .withMetadata(context.node().getMetadata())
                        .withHint(de.uni_passau.fim.se2.litterbox.analytics.Hint.fromKey(getName()));
                
                if (context.script() != null) {
                    builder.withScript(context.script());
                } else if (context.procedure() != null) {
                    builder.withProcedure(context.procedure());
                }
                
                issues.add(builder.build());
            }
        });

        return issues;
    }
}
