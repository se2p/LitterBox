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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the InterruptedLoopSensing bug.
 *
 * <p>Uses "existence check" strategy - if the interrupting block still exists,
 * but the bug finder doesn't report it, it's considered fixed.
 */
public class InterruptedLoopSensingFix extends AbstractFixHeuristic {

    private final InterruptedLoopSensing relatedBugFinder = new InterruptedLoopSensing();

    public InterruptedLoopSensingFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node) && !isBugStillPresent(node)) {
            addFix(node, node.getMetadata());
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(ThinkForSecs node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(SayForSecs node) {
        check(node);
        visitChildren(node);
    }
}
