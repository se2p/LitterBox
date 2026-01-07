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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the RecursiveCloning bug.
 *
 * <p>The bug is reported when "When I start as clone" creates a clone of myself (infinite loop).
 * The bug is fixed if DeleteClone is present in the same script, breaking the recursion.
 */
public class RecursiveCloningFix extends AbstractFixHeuristic {

    private final RecursiveCloning relatedBugFinder = new RecursiveCloning();

    public RecursiveCloningFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(Script node) {
        if (!(node.getEvent() instanceof StartedAsClone)) {
            return;
        }

        // Single-pass: Check if DeleteClone exists and if the target CreateCloneOf is present
        ScriptAnalyzer analyzer = new ScriptAnalyzer();
        node.accept(analyzer);

        // If the script contains DeleteClone, the recursive cloning is fixed
        if (analyzer.hasDeleteClone && analyzer.targetCreateClone != null) {
            addFix(analyzer.targetCreateClone, analyzer.targetCreateClone.getMetadata());
        }
    }

    /**
     * Single-pass analyzer that collects information about a script.
     */
    private class ScriptAnalyzer implements ScratchVisitor {
        boolean hasDeleteClone = false;
        CreateCloneOf targetCreateClone = null;

        @Override
        public void visit(DeleteClone node) {
            hasDeleteClone = true;
            // Continue traversal in case target is found later
        }

        @Override
        public void visit(CreateCloneOf node) {
            if (matchesBugLocation(node)) {
                targetCreateClone = node;
            }
            visitChildren(node);
        }
    }
}
