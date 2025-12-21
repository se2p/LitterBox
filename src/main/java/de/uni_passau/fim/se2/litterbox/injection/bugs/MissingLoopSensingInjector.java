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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injector for the {@link MissingLoopSensing} bug.
 * <p>
 * The MissingLoopSensing bug occurs when an if-condition checks an event (like key pressed,
 * touching, etc.) without being embedded in a loop. This injector finds if-statements with
 * sensing conditions that ARE inside loops, and removes the surrounding loop to expose the bug.
 */
public class MissingLoopSensingInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingLoopSensing();
    }

    @Override
    public void visit(StmtList stmtList) {
        List<Stmt> statements = stmtList.getStmts();
        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            if (stmt instanceof LoopStmt loopStmt) {
                IfStmt sensingIf = findIfWithSensingCondition(loopStmt);
                if (sensingIf != null) {
                    addUnwrapLoopMutation(stmtList, i, loopStmt, sensingIf);
                }
            }
        }
        super.visit(stmtList);
    }

    /**
     * Finds an if-statement inside the loop that has a sensing condition.
     */
    private IfStmt findIfWithSensingCondition(LoopStmt loopStmt) {
        StmtList body = getLoopBody(loopStmt);
        if (body == null) {
            return null;
        }

        SensingIfFinder finder = new SensingIfFinder();
        body.accept(finder);
        return finder.getSensingIf();
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
     * <p>
     * Before: [forever { if <key pressed?> { ... }, stmt2, ... }]
     * After:  [if <key pressed?> { ... }, stmt2, ...]
     * <p>
     * This creates a bug because the sensing check is no longer continuously evaluated.
     */
    private void addUnwrapLoopMutation(StmtList stmtList, int index, LoopStmt loopStmt, IfStmt sensingIf) {
        mutateClone(stmtList, clonedList -> {
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
     * Visitor that finds an if-statement with a sensing condition.
     */
    private class SensingIfFinder implements ScratchVisitor {
        private IfStmt sensingIf = null;

        @Override
        public void visit(IfThenStmt node) {
            if (sensingIf == null && hasSensingCondition(node.getBoolExpr())) {
                sensingIf = node;
            }
        }

        @Override
        public void visit(IfElseStmt node) {
            if (sensingIf == null && hasSensingCondition(node.getBoolExpr())) {
                sensingIf = node;
            }
        }

        public IfStmt getSensingIf() {
            return sensingIf;
        }
    }

    /**
     * Visitor that checks if an expression contains a sensing condition.
     */
    private static class SensingConditionChecker implements ScratchVisitor {
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

        public boolean hasSensingCondition() {
            return found;
        }
    }
}
