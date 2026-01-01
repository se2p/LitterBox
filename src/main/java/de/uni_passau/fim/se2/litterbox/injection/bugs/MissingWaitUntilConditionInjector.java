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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingWaitUntilCondition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Injects the MissingWaitUntilCondition bug by replacing WaitUntil conditions with unspecified.
 *
 * <p>
 * Preconditions:
 * <ul>
 *   <li>The condition is not already unspecified</li>
 * </ul>
 *
 * <p>
 * Note: Uses index-based access to avoid indexOf() issues with multiple structurally-equal nodes.
 */
public class MissingWaitUntilConditionInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MissingWaitUntilCondition();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> statements = node.getStmts();

        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            if (stmt instanceof WaitUntil waitUntil && shouldInject(waitUntil)) {
                injectAtIndex(node, i);
            }
        }

        super.visit(node);
    }

    private void injectAtIndex(StmtList stmtList, int index) {
        modifyClonedNode(stmtList, clonedList -> {
            List<Stmt> stmts = clonedList.getStmts();
            if (index < stmts.size() && stmts.get(index) instanceof WaitUntil clonedWait) {
                WaitUntil newWait = nodeFactory.createWaitUntil()
                        .withCondition(nodeFactory.unspecifiedBool())
                        .withMetadata(clonedWait.getMetadata())
                        .build();

                // Set parents
                newWait.getUntil().setParentNode(newWait);
                newWait.setParentNode(clonedList);

                stmts.set(index, newWait);
            }
        });
    }

    private boolean shouldInject(WaitUntil node) {
        // Skip if condition is already unspecified
        return !(node.getUntil() instanceof UnspecifiedBoolExpr);
    }
}
