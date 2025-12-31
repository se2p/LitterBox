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

import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithForever;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

/**
 * Fix heuristic for the CustomBlockWithForever bug.
 *
 * <p>The bug is reported when a call to a procedure containing RepeatForever
 * is not in the last position, blocking subsequent code.
 * The bug is fixed if the procedure no longer contains forever, or the call is now last.
 */
public class CustomBlockWithForeverFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new CustomBlockWithForever();

    public CustomBlockWithForeverFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> check(de.uni_passau.fim.se2.litterbox.ast.model.Program program) {
        this.program = program;
        this.issues = new java.util.LinkedHashSet<>();
        if (bugLocationBlockId != null && de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            addIssueWithLooseComment();
            return java.util.Collections.unmodifiableSet(issues);
        }
        return super.check(program);
    }

    @Override
    public void visit(CallStmt node) {
        if (matchesBugLocation(node) && !isBugStillPresent(node)) {
            addIssue(node, node.getMetadata());
        }
        super.visit(node);
    }
}
