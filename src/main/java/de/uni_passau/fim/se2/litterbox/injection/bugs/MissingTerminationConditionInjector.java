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

