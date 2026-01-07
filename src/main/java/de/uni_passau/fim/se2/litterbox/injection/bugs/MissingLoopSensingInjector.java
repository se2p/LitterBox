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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injector for the {@link MissingLoopSensing} bug.
 *
 * <p>
 * The MissingLoopSensing bug occurs when an if-condition checks an event (like key pressed,
 * touching, etc.) without being embedded in a loop. This injector finds if-statements with
 * sensing conditions that ARE inside loops, and removes the surrounding loop to expose the bug.
 */
public class MissingLoopSensingInjector extends AbstractBugInjector {

    private boolean inValidScript = false;

    private final IssueFinder issueFinder = new MissingLoopSensing();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Script node) {
        // Only process scripts with GreenFlag or StartedAsClone events (matching finder behavior)
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inValidScript = true;
            super.visit(node);
            inValidScript = false;
        }
    }

    @Override
    public void visit(StmtList stmtList) {
        // Only process if we're in a valid script (GreenFlag or StartedAsClone)
        if (!inValidScript) {
            super.visit(stmtList);
            return;
        }

        // Track if we've seen WaitUntil or AskAndWait in this StmtList
        // The finder sets afterWaitUntil/afterAskAndWait for the ENTIRE script,
        // so any WaitUntil before the loop will prevent detection
        boolean seenWaitUntilInParent = false;
        boolean seenAskAndWaitInParent = false;

        List<Stmt> statements = stmtList.getStmts();
        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);

            // Track WaitUntil and AskAndWait in the parent StmtList
            if (stmt instanceof WaitUntil) {
                seenWaitUntilInParent = true;
            }
            if (stmt instanceof AskAndWait) {
                seenAskAndWaitInParent = true;
            }

            if (stmt instanceof LoopStmt loopStmt) {
                // Skip if WaitUntil or AskAndWait appeared before this loop
                // because the finder won't detect sensing after those
                if (seenWaitUntilInParent || seenAskAndWaitInParent) {
                    continue;
                }

                // Only target outermost loops - nested loops won't expose the bug
                if (isInsideLoop(loopStmt)) {
                    continue;
                }
                IfStmt sensingIf = findIfWithSensingCondition(loopStmt);
                if (sensingIf != null) {
                    addUnwrapLoopMutation(stmtList, i);
                }
            }
        }
        super.visit(stmtList);
    }

    /**
     * Finds an if-statement that is a direct child of the loop and has a sensing condition.
     * Only direct children are checked to avoid removing loops that contain nested loops or
     * ifs with sensing inside other ifs.
     *
     * <p>Additionally, we skip loops that contain WaitUntil or AskAndWait statements before
     * the sensing if-statement, because the finder won't flag sensing conditions after these.
     */
    private IfStmt findIfWithSensingCondition(LoopStmt loopStmt) {
        StmtList body = getLoopBody(loopStmt);
        if (body == null) {
            return null;
        }

        // Track if we've seen WaitUntil or AskAndWait - finder skips sensing after these
        boolean seenWaitUntil = false;
        boolean seenAskAndWait = false;

        // Only check direct children, not nested structures
        for (Stmt stmt : body.getStmts()) {
            // Check for WaitUntil or AskAndWait which would prevent detection
            if (stmt instanceof WaitUntil) {
                seenWaitUntil = true;
            }
            if (stmt instanceof AskAndWait) {
                seenAskAndWait = true;
            }

            // Only return sensing if-statements that come BEFORE any WaitUntil/AskAndWait
            // (finder sets afterWaitUntil/afterAskAndWait and skips sensing after those)
            if (!seenWaitUntil && !seenAskAndWait) {
                if (stmt instanceof IfThenStmt ifThen && hasSensingCondition(ifThen.getBoolExpr())) {
                    return ifThen;
                }
                if (stmt instanceof IfElseStmt ifElse && hasSensingCondition(ifElse.getBoolExpr())) {
                    return ifElse;
                }
            }
        }
        return null;
    }

    private StmtList getLoopBody(LoopStmt loopStmt) {
        if (loopStmt instanceof RepeatForeverStmt forever) {
            return forever.getStmtList();
        } else if (loopStmt instanceof UntilStmt until) {
            return until.getStmtList();
        }
        return null;
    }

    /**
     * Adds a mutation that removes the loop and keeps all body statements.
     *
     * <p>Before: [forever { if &lt;key pressed?&gt; { ... }, stmt2, ... }]
     * After:  [if &lt;key pressed?&gt; { ... }, stmt2, ...]
     *
     * <p>This creates a bug because the sensing check is no longer continuously evaluated.
     */
    private void addUnwrapLoopMutation(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
            if (index < clonedList.getStmts().size()
                    && clonedList.getStmts().get(index) instanceof LoopStmt clonedLoop) {

                StmtList loopBody = getLoopBody(clonedLoop);
                if (loopBody != null) {
                    List<Stmt> bodyStmts = loopBody.getStmts();

                    // Remove the loop
                    clonedList.getStmts().remove(index);

                    // Insert ALL body statements at the same position
                    int insertIdx = index;
                    for (Stmt bodyStmt : bodyStmts) {
                        bodyStmt.setParentNode(clonedList);
                        clonedList.getStmts().add(insertIdx++, bodyStmt);
                    }
                }
            }
        });
    }

    /**
     * Checks if a boolean expression is a sensing condition.
     */
    private boolean hasSensingCondition(BoolExpr expr) {
        SensingConditionChecker checker = new SensingConditionChecker();
        expr.accept(checker);
        return checker.hasSensingCondition();
    }

    /**
     * Visitor that checks if an expression contains a sensing condition.
     */
    private static class SensingConditionChecker implements ScratchVisitor, VideoSensingExtensionVisitor {
        private boolean found = false;

        @Override
        public void visit(Touching node) {
            found = true;
        }

        @Override
        public void visit(ColorTouchingColor node) {
            found = true;
        }

        @Override
        public void visit(SpriteTouchingColor node) {
            found = true;
        }

        @Override
        public void visit(IsKeyPressed node) {
            found = true;
        }

        @Override
        public void visit(IsMouseDown node) {
            found = true;
        }

        @Override
        public void visit(DistanceTo node) {
            found = true;
        }

        @Override
        public void visit(VideoOn node) {
            found = true;
        }

        public boolean hasSensingCondition() {
            return found;
        }
    }
}
