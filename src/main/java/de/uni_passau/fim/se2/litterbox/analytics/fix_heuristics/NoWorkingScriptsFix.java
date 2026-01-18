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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the NoWorkingScripts bug.
 *
 * <p>The bug is reported when an actor has BOTH:
 * <ul>
 *   <li>Dead code: a script with no event handler (Never) but with statements</li>
 *   <li>Empty script: an event handler with no statements attached</li>
 * </ul>
 *
 * <p>The bug is fixed if the actor no longer has both conditions simultaneously.
 */
public class NoWorkingScriptsFix extends AbstractFixHeuristic {

    private boolean hasEmptyEventScript;
    private boolean deadCodeFound;
    private boolean foundEvent;

    private final NoWorkingScripts relatedBugFinder = new NoWorkingScripts();

    public NoWorkingScriptsFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    protected void resetState() {
        hasEmptyEventScript = false;
        deadCodeFound = false;
        foundEvent = false;
    }

    @Override
    public void visit(ActorDefinition actor) {
        // Match by actor name since this bug has no block ID
        if (!actor.getIdent().getName().equals(originalIssue.sprite())) {
            return;
        }

        hasEmptyEventScript = true;
        deadCodeFound = false;
        foundEvent = false;

        // Traverse this actor's scripts
        super.visit(actor);

        // Bug condition: deadCode AND emptyScript AND foundEvent
        boolean bugPresent = deadCodeFound && hasEmptyEventScript && foundEvent;

        if (!bugPresent) {
            // Fixed!
            addFixWithLooseComment(actor);
        }
    }

    @Override
    public void visit(Script node) {
        currentScript = node;

        // Replicate bug finder logic exactly
        if (hasEmptyEventScript) {
            if (node.getEvent() instanceof Never) {
                // Script without event handler
                if (!node.getStmtList().getStmts().isEmpty()) {
                    deadCodeFound = true;
                }
            } else {
                // Script with event handler
                foundEvent = true;
                if (!node.getStmtList().getStmts().isEmpty()) {
                    hasEmptyEventScript = false;
                }
            }
        }

        visitChildren(node);
        currentScript = null;
    }
}
