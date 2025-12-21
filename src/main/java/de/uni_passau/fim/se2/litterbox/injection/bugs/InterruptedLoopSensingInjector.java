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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the InterruptedLoopSensing bug by inserting a WaitSeconds(1) block into a responsive loop.
 * <p>
 * Only applies to loops where sensing is in a control condition (if-statement or until-loop condition),
 * not inside a WaitUntil statement (which already blocks until the condition is true).
 */
public class InterruptedLoopSensingInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new InterruptedLoopSensing();
    }

    private void inject(LoopStmt originalLoop) {
        if (containsSensingInControlCondition(originalLoop)) {
            mutateClone(originalLoop, clonedLoop -> {
                WaitSeconds waitStmt = nodeFactory.createWaitSeconds(1.0);
                clonedLoop.getStmtList().getStmts().add(waitStmt);
                waitStmt.setParentNode(clonedLoop.getStmtList());
            });
        }
    }

    private boolean containsSensingInControlCondition(LoopStmt loop) {
        SensingFinder finder = new SensingFinder();
        loop.accept(finder);
        return finder.foundSensing;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        inject(node);
        super.visit(node);
    }

    @Override
    public void visit(UntilStmt node) {
        inject(node);
        super.visit(node);
    }

    /**
     * Visitor that checks if an AST contains sensing blocks in control conditions.
     * Sensing inside WaitUntil is excluded (WaitUntil already blocks properly).
     */
    private static class SensingFinder implements ScratchVisitor {
        boolean foundSensing = false;
        boolean insideWaitUntil = false;

        private void markFound() {
            // Only count sensing if not inside a WaitUntil
            if (!insideWaitUntil) {
                foundSensing = true;
            }
        }

        @Override
        public void visit(WaitUntil node) {
            // Skip sensing inside WaitUntil - it already blocks properly
            insideWaitUntil = true;
            visitChildren(node);
            insideWaitUntil = false;
        }

        @Override
        public void visit(IfThenStmt node) {
            visitChildren(node);
        }

        @Override
        public void visit(IfElseStmt node) {
            visitChildren(node);
        }

        @Override
        public void visit(UntilStmt node) {
            visitChildren(node);
        }

        @Override
        public void visit(Touching node) {
            markFound();
        }

        @Override
        public void visit(IsKeyPressed node) {
            markFound();
        }

        @Override
        public void visit(IsMouseDown node) {
            markFound();
        }

        @Override
        public void visit(ColorTouchingColor node) {
            markFound();
        }

        @Override
        public void visit(SpriteTouchingColor node) {
            markFound();
        }
    }
}
