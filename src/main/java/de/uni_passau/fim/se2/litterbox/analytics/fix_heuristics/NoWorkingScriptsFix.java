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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Set;

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
    
    private ASTNode targetNode;
    private Script targetScript;
    private ActorDefinition targetActorOfBug;

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
        stillFullfilledEmptyScript = false;
        deadCodeFound = false;
        foundEvent = false;
        targetNode = null;
        targetScript = null;
        targetActorOfBug = null;

        BlockIdNodeFinder.find(program, getBugLocationBlockId()).ifPresent(ctx -> {
            targetNode = ctx.node();
            targetScript = ctx.script();
            targetActorOfBug = ctx.actor();
        });
    }

    @Override
    public Set<FixResult> check(Program program) {
        return super.check(program);
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (targetActorOfBug == null || actor != targetActorOfBug) {
            return;
        }
        
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        foundEvent = false;

        // Traverse this actor's scripts
        super.visit(actor);

        // Bug condition: deadCode AND emptyScript AND foundEvent
        boolean bugPresent = deadCodeFound && stillFullfilledEmptyScript && foundEvent;

        if (!bugPresent) {
            // Fixed!
            currentActor = targetActorOfBug;
            currentScript = targetScript;
            addFix(targetNode, targetNode.getMetadata());
        }
    }

    @Override
    public void visit(Script node) {
        currentScript = node;

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

        visitChildren(node);
        currentScript = null;
    }
}
