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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects ForeverInsideIf bug by wrapping the contents of an if-statement's
 * then-block (or else-block) in a forever loop.
 * <p>
 * Conditions for injection:
 * - The if-statement must have successor statements (not be the last in its block)
 * - The block being wrapped must not be empty
 * - For IfElseStmt, both then-block and else-block are candidates (if non-empty)
 */
public class ForeverInsideIfInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ForeverInsideIf();
    }

    @Override
    public void visit(IfThenStmt node) {
        // Only inject if this if-statement has successor statements
        if (!hasSuccessorStatements(node)) {
            super.visit(node);
            return;
        }

        // Only inject if the then-block is not empty
        if (!node.getThenStmts().getStmts().isEmpty()) {
            addMutation(node, clonedNode -> {
                wrapInForever(clonedNode.getThenStmts());
            });
        }
        super.visit(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        // Only inject if this if-statement has successor statements
        if (!hasSuccessorStatements(node)) {
            super.visit(node);
            return;
        }

        // Wrap then-block if not empty
        if (!node.getThenStmts().getStmts().isEmpty()) {
            addMutation(node, clonedNode -> {
                wrapInForever(clonedNode.getThenStmts());
            });
        }

        // Also consider wrapping else-block if not empty
        if (!node.getElseStmts().getStmts().isEmpty()) {
            addMutation(node, clonedNode -> {
                wrapInForever(clonedNode.getElseStmts());
            });
        }

        super.visit(node);
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
