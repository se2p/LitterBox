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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Collections;

import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the MissingInitialization bug.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 *
 * <p>The bug is fixed if the variable use is now properly initialized before use.
 */
public class MissingInitializationFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new MissingInitialization();

    public MissingInitializationFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public Set<Issue> check(Program program) {
        super.check(program);

        // Find the target node
        Optional<BlockIdNodeFinder.NodeContext> contextOpt =
                BlockIdNodeFinder.find(program, bugLocationBlockId);

        if (contextOpt.isEmpty()) {
            addIssueWithLooseComment();
            return Collections.unmodifiableSet(issues);
        }

        contextOpt.ifPresent(context -> {
            if (!isBugStillPresent(context.node())) {
                // Fixed! The use is now properly initialized (or removed).
                IssueBuilder builder = new IssueBuilder()
                        .withFinder(this)
                        .withCurrentNode(context.node())
                        .withActor(context.actor())
                        .withProgram(program)
                        .withMetadata(context.node().getMetadata())
                        .withHint(getName());

                if (context.script() != null) {
                    builder.withScript(context.script());
                }
                if (context.procedure() != null) {
                    builder.withProcedure(context.procedure());
                }

                issues.add(builder.build());
            }
        });

        return issues;
    }
}
