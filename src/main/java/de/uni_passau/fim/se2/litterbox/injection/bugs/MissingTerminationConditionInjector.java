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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingTerminationCondition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the MissingTerminationCondition bug by replacing UntilStmt conditions with false.
 * <p>
 * Preconditions:
 * <ul>
 *   <li>The condition is not already unspecified/false</li>
 *   <li>The loop has successor statements (otherwise no code would be blocked)</li>
 * </ul>
 */
public class MissingTerminationConditionInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingTerminationCondition();
    }

    @Override
    public void visit(UntilStmt node) {
        if (shouldInject(node)) {
            addMutation(node, clonedUntil -> {
                UntilStmt newUntil = nodeFactory.createUntil()
                        .withCondition(nodeFactory.unspecifiedBool())
                        .withBody(clonedUntil.getStmtList())
                        .withMetadata(clonedUntil.getMetadata())
                        .build();

                // Set parents
                newUntil.getBoolExpr().setParentNode(newUntil);
                newUntil.getStmtList().setParentNode(newUntil);

                // Replace in parent StmtList
                var parent = clonedUntil.getParentNode();
                if (parent instanceof StmtList stmtList) {
                    int index = stmtList.getStmts().indexOf(clonedUntil);
                    if (index != -1) {
                        newUntil.setParentNode(stmtList);
                        stmtList.getStmts().set(index, newUntil);
                    }
                }
            });
        }
        super.visit(node);
    }

    private boolean shouldInject(UntilStmt node) {
        // Skip if condition is already unspecified/false
        if (node.getBoolExpr() instanceof UnspecifiedBoolExpr) {
            return false;
        }

        // Skip if loop has no successors (no code would be blocked by infinite loop)
        if (node.getParentNode() instanceof StmtList stmtList) {
            int index = stmtList.getStmts().indexOf(node);
            return index != stmtList.getStmts().size() - 1;
        }

        return true;
    }
}

