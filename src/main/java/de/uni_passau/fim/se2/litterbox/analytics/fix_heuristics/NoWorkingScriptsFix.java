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

import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

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

    private boolean stillFullfilledEmptyScript;
    private boolean deadCodeFound;
    private boolean foundEvent;
    private ActorDefinition targetActor;

    private final IssueFinder relatedBugFinder = new NoWorkingScripts();

    public NoWorkingScriptsFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(ActorDefinition actor) {
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        foundEvent = false;

        // First pass: traverse to find the target actor and check conditions
        super.visit(actor);

        // Check if this is the actor containing our bug location
        if (targetActor == actor) {
            // Bug condition: deadCode AND emptyScript AND foundEvent
            boolean bugPresent = deadCodeFound && stillFullfilledEmptyScript && foundEvent;

            if (!bugPresent) {
                // Fixed! The actor no longer has the bug condition.
                issues.add(new IssueBuilder()
                        .withFinder(this)
                        .withSeverity(IssueSeverity.HIGH)
                        .withProgram(program)
                        .withActor(actor)
                        .withMetadata(actor.getActorMetadata())
                        .withHint(Hint.fromKey(getName()))
                        .build());
            }
        }
    }

    @Override
    public void visit(Script node) {
        currentScript = node;

        // Check if this script or its blocks contain the bug location ID
        if (containsBugLocation(node)) {
            targetActor = currentActor;
        }

        // Replicate bug finder logic exactly
        if (stillFullfilledEmptyScript) {
            if (node.getEvent() instanceof Never) {
                // Script without event handler
                if (!node.getStmtList().getStmts().isEmpty()) {
                    deadCodeFound = true;
                }
            } else {
                // Script with event handler
                foundEvent = true;
                if (!node.getStmtList().getStmts().isEmpty()) {
                    stillFullfilledEmptyScript = false;
                }
            }
        }

        currentScript = null;
    }

    private boolean containsBugLocation(Script script) {
        if (AstNodeUtil.hasBlockId(script, bugLocationBlockId)) {
            return true;
        }
        // Check recursively in children
        return containsBugLocationRecursive(script);
    }

    private boolean containsBugLocationRecursive(de.uni_passau.fim.se2.litterbox.ast.model.ASTNode node) {
        if (AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
            return true;
        }
        for (de.uni_passau.fim.se2.litterbox.ast.model.ASTNode child : node.getChildren()) {
            if (containsBugLocationRecursive(child)) {
                return true;
            }
        }
        return false;
    }
}
