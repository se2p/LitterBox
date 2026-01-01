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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Injects ForeverInsideIf bug by wrapping the contents of an if-statement's
 * then-block (or else-block) in a forever loop.
 *
 * <p>
 * Conditions for injection:
 * - The if-statement must have successor statements OR be inside a loop
 * (loops implicitly have successor statements due to repetition)
 * - The block being wrapped must not be empty
 * - The block must not end with a terminating statement (stop/delete clone)
 * - For IfElseStmt, both then-block and else-block are candidates (if non-empty)
 */
public class ForeverInsideIfInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ForeverInsideIf();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(IfThenStmt node) {
        // Only inject if this if-statement has successor statements OR is inside a loop
        if (!hasSuccessorOrIsInLoop(node)) {
            super.visit(node);
            return;
        }

        // Only inject if the then-block is suitable for wrapping
        if (isSuitableForWrapping(node.getThenStmts())) {
            modifyClonedNode(node, clonedNode -> {
                wrapInForever(clonedNode.getThenStmts());
            });
        }
        super.visit(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        // Only inject if this if-statement has successor statements OR is inside a loop
        if (!hasSuccessorOrIsInLoop(node)) {
            super.visit(node);
            return;
        }

        // Wrap then-block if suitable
        if (isSuitableForWrapping(node.getThenStmts())) {
            modifyClonedNode(node, clonedNode -> {
                wrapInForever(clonedNode.getThenStmts());
            });
        }

        // Also consider wrapping else-block if suitable
        if (isSuitableForWrapping(node.getElseStmts())) {
            modifyClonedNode(node, clonedNode -> {
                wrapInForever(clonedNode.getElseStmts());
            });
        }

        super.visit(node);
    }

    /**
     * Checks if a statement list is suitable for wrapping in a forever loop.
     * Returns false if the list is empty or ends with a terminating statement.
     */
    private boolean isSuitableForWrapping(StmtList stmtList) {
        List<Stmt> stmts = stmtList.getStmts();
        if (stmts.isEmpty()) {
            return false;
        }
        // Check if the last statement is a terminating statement
        Stmt lastStmt = stmts.getLast();
        return !isTerminatingStatement(lastStmt);
    }

    /**
     * Statement types that block execution (no point wrapping after these).
     */
    private static final Set<Class<? extends Stmt>> TERMINATING_TYPES = Set.of(
            StopAll.class, StopThisScript.class, DeleteClone.class
    );

    private boolean isTerminatingStatement(Stmt stmt) {
        return TERMINATING_TYPES.contains(stmt.getClass());
    }

    private boolean hasSuccessorOrIsInLoop(Stmt stmt) {
        return hasSuccessorStatements(stmt) || isInsideLoop(stmt);
    }

    private void wrapInForever(StmtList targetList) {
        List<Stmt> originalStmts = new ArrayList<>(targetList.getStmts());
        targetList.getStmts().clear();

        StmtList newBody = new StmtList(originalStmts);
        for (Stmt s : originalStmts) {
            s.setParentNode(newBody);
        }

        RepeatForeverStmt forever = nodeFactory.createForever(newBody);
        newBody.setParentNode(forever);

        targetList.getStmts().add(forever);
        forever.setParentNode(targetList);
    }
}

