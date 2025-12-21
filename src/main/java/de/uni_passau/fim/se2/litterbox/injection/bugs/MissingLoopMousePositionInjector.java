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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopMousePosition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;
import java.util.Set;

/**
 * Injector for the {@link MissingLoopMousePosition} bug.
 * <p>
 * This injector finds loops containing motion statements that depend on mouse position
 * (MouseX, MouseY, or MousePos) and removes the loop wrapper, "freezing" the mouse tracking.
 */
public class MissingLoopMousePositionInjector extends AbstractBugInjector {

    private static final Set<String> MOUSE_POINTER_IDENTIFIERS = Set.of("_mouse_", "mouse-pointer");

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingLoopMousePosition();
    }

    @Override
    public void visit(StmtList stmtList) {
        List<Stmt> statements = stmtList.getStmts();
        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            if (isTargetLoop(stmt) && hasMouseDependency((LoopStmt) stmt)) {
                addUnwrapLoopMutation(stmtList, i);
            }
        }
        super.visit(stmtList);
    }

    private boolean isTargetLoop(Stmt stmt) {
        return stmt instanceof RepeatForeverStmt || stmt instanceof UntilStmt;
    }

    /**
     * Adds a mutation that unwraps a loop by replacing it with its body statements.
     * <p>
     * Before: [stmt1, LOOP { bodyA, bodyB }, stmt2]
     * After:  [stmt1, bodyA, bodyB, stmt2]
     *
     * @param stmtList the statement list containing the loop
     * @param index    the index of the loop statement to unwrap
     */
    private void addUnwrapLoopMutation(StmtList stmtList, int index) {
        addMutation(stmtList, clonedList -> {
            if (index < clonedList.getStmts().size()
                    && clonedList.getStmts().get(index) instanceof LoopStmt clonedLoop) {
                List<Stmt> bodyStmts = clonedLoop.getStmtList().getStmts();

                // Remove the loop statement
                clonedList.getStmts().remove(index);

                // Insert the loop's body statements at the same position
                int insertIdx = index;
                for (Stmt bodyStmt : bodyStmts) {
                    bodyStmt.setParentNode(clonedList);
                    clonedList.getStmts().add(insertIdx++, bodyStmt);
                }
            }
        });
    }

    private boolean hasMouseDependency(LoopStmt loopStmt) {
        MouseDependencyVisitor visitor = new MouseDependencyVisitor();
        loopStmt.getStmtList().accept(visitor);
        return visitor.foundMouseDependency;
    }

    /**
     * Visitor that checks if motion statements use MouseX, MouseY, or MousePos.
     */
    private static class MouseDependencyVisitor implements ScratchVisitor {
        boolean foundMouseDependency = false;

        // Motion statements that use numeric X/Y coordinates
        @Override
        public void visit(SetXTo node) {
            checkForMouseXY(node);
        }

        @Override
        public void visit(SetYTo node) {
            checkForMouseXY(node);
        }

        @Override
        public void visit(GoToPosXY node) {
            checkForMouseXY(node);
        }

        @Override
        public void visit(GlideSecsToXY node) {
            checkForMouseXY(node);
        }

        // Motion statements that use a Position target
        @Override
        public void visit(PointTowards node) {
            checkPosition(node.getPosition());
        }

        @Override
        public void visit(GlideSecsTo node) {
            checkPosition(node.getPosition());
        }

        @Override
        public void visit(GoToPos node) {
            checkPosition(node.getPosition());
        }

        private void checkForMouseXY(ASTNode node) {
            MouseXYChecker checker = new MouseXYChecker();
            node.accept(checker);
            if (checker.foundMouseXY) {
                foundMouseDependency = true;
            }
        }

        private void checkPosition(Position pos) {
            if (pos instanceof MousePos) {
                foundMouseDependency = true;
            } else if (pos instanceof FromExpression fromExpr
                    && fromExpr.getStringExpr() instanceof StringLiteral strLit
                    && MOUSE_POINTER_IDENTIFIERS.contains(strLit.getText())) {
                foundMouseDependency = true;
            }
        }
    }

    /**
     * Simple visitor to detect MouseX or MouseY within a subtree.
     */
    private static class MouseXYChecker implements ScratchVisitor {
        boolean foundMouseXY = false;

        @Override
        public void visit(MouseX node) {
            foundMouseXY = true;
        }

        @Override
        public void visit(MouseY node) {
            foundMouseXY = true;
        }
    }
}

