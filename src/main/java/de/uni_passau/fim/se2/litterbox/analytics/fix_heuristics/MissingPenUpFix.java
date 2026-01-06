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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenUp;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the MissingPenUp bug.
 *
 * <p>The bug is reported when an actor uses PenDown but never PenUp.
 * The bug is fixed if the actor now also has a PenUp statement.
 */
public class MissingPenUpFix extends AbstractFixHeuristic implements PenExtensionVisitor {

    private boolean hasPenUpStatement;
    private PenDownStmt targetPenDown;
    private Script foundScript;

    private final MissingPenUp relatedBugFinder = new MissingPenUp();

    public MissingPenUpFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        hasPenUpStatement = false;
        targetPenDown = null;
        foundScript = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        super.visit(actor);

        if (targetPenDown != null && hasPenUpStatement) {
            // Fix: PenUp is present in the actor. Use explicit context overload.
            addFix(targetPenDown, targetPenDown.getMetadata(), actor, foundScript);
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
        hasPenUpStatement = true;
        visitChildren(node);
    }
}
