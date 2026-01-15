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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Stack;

/**
 * Fix heuristic for the MissingLoopSensing bug.
 *
 * <p>The bug is reported when sensing blocks are used outside a loop.
 * The bug is fixed if they're now inside a loop or after a wait/ask.
 */
public class MissingLoopSensingFix extends AbstractFixHeuristic implements VideoSensingExtensionVisitor {

    private final IssueFinder relatedBugFinder = new MissingLoopSensing();

    private Stack<LoopStmt> loopStack;
    private boolean afterWaitUntil;
    private boolean afterAskAndWait;

    @Override
    protected void resetState() {
        loopStack = new Stack<>();
        afterWaitUntil = false;
        afterAskAndWait = false;
    }

    public MissingLoopSensingFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(Script node) {
        // Flags reset at start of overall check via resetState(),
        // but we reset them here for each script to ensure proper scoping
        // within the same program traversal.
        afterWaitUntil = false;
        afterAskAndWait = false;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    @Override
    public void visit(UntilStmt node) {
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    @Override
    public void visit(WaitUntil node) {
        afterWaitUntil = true;
        visitChildren(node);
    }

    @Override
    public void visit(AskAndWait node) {
        afterAskAndWait = true;
        visitChildren(node);
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node)) {
            // Fix: Inside a loop OR after a wait/ask
            if (!loopStack.isEmpty() || afterWaitUntil || afterAskAndWait) {
                addFix(node, node.getMetadata());
            }
        }
    }

    /**
     * Common pattern for sensing expression visitors: check location and visit children.
     */
    private void checkAndVisitChildren(ASTNode node) {
        check(node);
        visitChildren(node);
    }

    @Override
    public void visit(IsKeyPressed node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(Touching node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        checkAndVisitChildren(node);
    }

    @Override
    public void visit(VideoOn node) {
        checkAndVisitChildren(node);
    }
}

