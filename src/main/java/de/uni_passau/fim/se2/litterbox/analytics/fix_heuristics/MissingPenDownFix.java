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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenDown;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the MissingPenDown bug.
 *
 * <p>The bug is reported when an actor uses PenUp but never PenDown.
 * The bug is fixed if the actor now also has a PenDown statement.
 */
public class MissingPenDownFix extends AbstractFixHeuristic implements PenExtensionVisitor {

    private boolean hasPenDownStatement;
    private PenUpStmt targetPenUp;
    private Script foundScript;

    private final MissingPenDown relatedBugFinder = new MissingPenDown();

    public MissingPenDownFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        hasPenDownStatement = false;
        targetPenUp = null;
        foundScript = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        super.visit(actor);

        if (targetPenUp != null && hasPenDownStatement) {
            // Fix: PenDown is present in the actor. Use explicit context overload.
            addFix(targetPenUp, targetPenUp.getMetadata(), actor, foundScript);
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (matchesBugLocation(node)) {
            targetPenUp = node;
            foundScript = currentScript;
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenDownStmt node) {
        hasPenDownStatement = true;
        visitChildren(node);
    }
}
