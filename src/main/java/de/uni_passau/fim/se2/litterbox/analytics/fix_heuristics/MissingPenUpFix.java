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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenUp;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.Collections;
import java.util.Set;

/**
 * Fix heuristic for the MissingPenUp bug.
 *
 * <p>The bug is reported when an actor uses PenDown but never PenUp.
 * The bug is fixed if the actor now also has a PenUp statement.
 */
public class MissingPenUpFix extends AbstractFixHeuristic implements PenExtensionVisitor {

    private boolean penUpSet;
    private PenDownStmt targetPenDown;
    private Script foundScript;

    private final IssueFinder relatedBugFinder = new MissingPenUp();

    public MissingPenUpFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<Issue> check(Program program) {
        // If the original buggy block (PenDown) is gone, we consider it fixed.
        if (bugLocationBlockId != null && BlockIdNodeFinder.find(program, bugLocationBlockId).isEmpty()) {
            addIssueWithLooseComment();
            return Collections.unmodifiableSet(issues);
        }
        return super.check(program);
    }


    @Override
    public void visit(ActorDefinition actor) {
        penUpSet = false;
        targetPenDown = null;
        foundScript = null;
        super.visit(actor);

        if (targetPenDown != null && penUpSet) {
            // Fix: PenUp is present in the actor.
            this.currentActor = actor;
            this.currentScript = foundScript;
            addIssue(targetPenDown, targetPenDown.getMetadata());
        }
    }

    @Override
    public void visit(PenDownStmt node) {
        if (matchesBugLocation(node)) {
            targetPenDown = node;
            foundScript = currentScript;
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenUpStmt node) {
        penUpSet = true;
        visitChildren(node);
    }
}
