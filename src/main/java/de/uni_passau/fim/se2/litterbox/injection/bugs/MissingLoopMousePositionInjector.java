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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopMousePosition;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;
import java.util.Set;

/**
 * Injector for the {@link MissingLoopMousePosition} bug.
 *
 * <p>
 * This injector finds loops containing motion statements that depend on mouse position
 * (MouseX, MouseY, or MousePos) and removes the loop wrapper, "freezing" the mouse tracking.
 */
public class MissingLoopMousePositionInjector extends AbstractBugInjector {

    private static final Set<String> MOUSE_POINTER_IDENTIFIERS = Set.of("_mouse_", "mouse-pointer");
    private boolean inValidScript = false;
    private boolean insideLoop = false;  // Track if we're inside a loop

    private final IssueFinder issueFinder = new MissingLoopMousePosition();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Script node) {
        // Only process scripts with GreenFlag or StartedAsClone events (matching finder behavior)
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inValidScript = true;
            insideLoop = false;
            super.visit(node);
            inValidScript = false;
        }
    }

    @Override
    public void visit(StmtList stmtList) {
        // Only process if we're in a valid script
        if (!inValidScript) {
            super.visit(stmtList);
            return;
        }

        List<Stmt> statements = stmtList.getStmts();
        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            // Only unwrap top-level loops (not loops inside other loops)
            // Unwrapping a nested loop would still leave the motion in an outer loop
            if (!insideLoop && isTargetLoop(stmt) && hasMouseDependency((LoopStmt) stmt)) {
                addUnwrapLoopMutation(stmtList, i);
            }
        }

        // Track when we enter a loop to avoid unwrapping nested loops
        for (Stmt stmt : statements) {
            if (stmt instanceof LoopStmt) {
                boolean wasInsideLoop = insideLoop;
                insideLoop = true;
                stmt.accept(this);
                insideLoop = wasInsideLoop;
            } else {
                stmt.accept(this);
            }
        }
    }

    private boolean isTargetLoop(Stmt stmt) {
        return stmt instanceof RepeatForeverStmt || stmt instanceof UntilStmt;
    }

    /**
     * Adds a mutation that unwraps a loop by replacing it with its body statements.
     *
     * <p>
     * Before: [stmt1, LOOP { bodyA, bodyB }, stmt2]
     * After:  [stmt1, bodyA, bodyB, stmt2]
     *
     * @param stmtList the statement list containing the loop
     * @param index    the index of the loop statement to unwrap
     */
    private void addUnwrapLoopMutation(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
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
     * Visitor that checks if motion statements DIRECTLY in a loop use MouseX, MouseY, or MousePos.
     * We skip nested loops because unwrapping the outer loop won't help if the motion is protected
     * by an inner loop.
     */
    private static class MouseDependencyVisitor implements ScratchVisitor {
        boolean foundMouseDependency = false;

        // Skip nested loops - mouse dependencies inside them won't be exposed
        @Override
        public void visit(RepeatForeverStmt node) {
            // Don't descend into nested forever loops
        }

        @Override
        public void visit(UntilStmt node) {
            // Don't descend into nested until loops
        }

        @Override
        public void visit(RepeatTimesStmt node) {
            // Don't descend into nested repeat-n-times loops
        }

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

