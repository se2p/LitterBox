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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the InterruptedLoopSensing bug by inserting a WaitSeconds(1) block into a control
 * structure (if/until) that contains sensing in its condition.
 *
 * <p>
 * The bug is injected inside the control structure's body (not at loop level), matching
 * what the InterruptedLoopSensing finder detects.
 *
 * <p>
 * Preconditions:
 * <ul>
 *   <li>The statement list doesn't already contain a blocking statement (wait, glide, etc.)</li>
 *   <li>The statement list doesn't contain stop statements (finder ignores these)</li>
 * </ul>
 */
public class InterruptedLoopSensingInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new InterruptedLoopSensing();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(IfThenStmt node) {
        // Target outermost if statements inside a forever loop
        if (isInsideForever(node) && !isNestedInControl(node)
                && shouldInject(node.getBoolExpr(), node.getThenStmts())) {
            modifyClonedNode(node, cloned -> injectWait(cloned.getThenStmts()));
        }
        super.visit(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        // Target if-else statements inside a forever loop (potentially nested)
        if (isInsideForever(node) && shouldInject(node.getBoolExpr(), node.getThenStmts(), node.getElseStmts())) {
            modifyClonedNode(node, cloned -> {
                injectWait(cloned.getThenStmts());
                injectWait(cloned.getElseStmts());
            });
        }
        super.visit(node);
    }

    @Override
    public void visit(UntilStmt node) {
        // Target until loops inside forever OR as top-level control structures
        if ((isInsideForever(node) || !isNestedInControl(node))
                && shouldInject(node.getBoolExpr(), node.getStmtList())) {
            modifyClonedNode(node, cloned -> injectWait(cloned.getStmtList()));
        }
        super.visit(node);
    }

    // ==================== Helper Methods ====================

    private boolean shouldInject(BoolExpr condition, StmtList... bodies) {
        for (StmtList body : bodies) {
            if (containsBlocking(body) || containsStop(body)) {
                return false;
            }
        }
        return containsSensing(condition);
    }

    private void injectWait(StmtList stmtList) {
        WaitSeconds wait = nodeFactory.createWaitSeconds(1.0);
        stmtList.getStmts().addFirst(wait);
        wait.setParentNode(stmtList);
    }

    private boolean isInsideForever(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof RepeatForeverStmt) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private boolean isNestedInControl(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof LoopStmt) {
                return false;
            }
            if (parent instanceof IfThenStmt || parent instanceof IfElseStmt || parent instanceof UntilStmt) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private boolean containsBlocking(StmtList list) {
        BlockingFinder finder = new BlockingFinder();
        list.accept(finder);
        return finder.foundBlocking;
    }

    private boolean containsStop(StmtList list) {
        StopFinder finder = new StopFinder();
        list.accept(finder);
        return finder.foundStop;
    }

    private boolean containsSensing(BoolExpr condition) {
        SensingFinder finder = new SensingFinder();
        condition.accept(finder);
        return finder.foundSensing;
    }

    /**
     * Visitor that checks if any statement in a list takes a longer time (blocking),
     * matching the types checked by the InterruptedLoopSensing finder.
     */
    private static class BlockingFinder implements ScratchVisitor {
        private boolean foundBlocking = false;

        @Override
        public void visit(WaitSeconds node) {
            foundBlocking = true;
        }

        @Override
        public void visit(GlideSecsTo node) {
            foundBlocking = true;
        }

        @Override
        public void visit(GlideSecsToXY node) {
            foundBlocking = true;
        }

        @Override
        public void visit(SayForSecs node) {
            foundBlocking = true;
        }

        @Override
        public void visit(ThinkForSecs node) {
            foundBlocking = true;
        }

        @Override
        public void visit(PlaySoundUntilDone node) {
            foundBlocking = true;
        }
    }

    /**
     * Visitor that checks if a statement list leads to a stop, matching the
     * recursive logic in InterruptedLoopSensing.checkForStop.
     */
    private static class StopFinder implements ScratchVisitor {
        private boolean foundStop = false;
        private boolean insideIfElse = false;

        @Override
        public void visit(StopAll node) {
            foundStop = true;
        }

        @Override
        public void visit(StopThisScript node) {
            foundStop = true;
        }

        @Override
        public void visit(RepeatForeverStmt node) {
            // InterruptedLoopSensing.checkForStop descends into forever loops
            visitChildren(node);
        }

        @Override
        public void visit(IfElseStmt node) {
            boolean wasInsideIfElse = insideIfElse;
            insideIfElse = true;

            node.getThenStmts().accept(this);
            boolean stopInThen = foundStop;
            foundStop = false; // Reset to check else

            node.getElseStmts().accept(this);
            boolean stopInElse = foundStop;

            foundStop = stopInThen || stopInElse;
            insideIfElse = wasInsideIfElse;
        }

        // Note: Finder skips IfThenStmt and UntilStmt for stop checking (matches non-guaranteed stop)
    }

    /**
     * Visitor that checks if an expression contains sensing blocks.
     * Sensing inside WaitUntil is excluded (WaitUntil already blocks properly).
     */
    private static class SensingFinder implements ScratchVisitor {
        private boolean foundSensing = false;
        private boolean insideWaitUntil = false;

        @Override
        public void visit(WaitUntil node) {
            insideWaitUntil = true;
            visitChildren(node);
            insideWaitUntil = false;
        }

        @Override
        public void visit(Touching node) {
            mark();
        }

        @Override
        public void visit(IsKeyPressed node) {
            mark();
        }

        @Override
        public void visit(IsMouseDown node) {
            mark();
        }

        @Override
        public void visit(ColorTouchingColor node) {
            mark();
        }

        @Override
        public void visit(SpriteTouchingColor node) {
            mark();
        }

        private void mark() {
            if (!insideWaitUntil) {
                foundSensing = true;
            }
        }
    }
}
