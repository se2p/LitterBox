package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.List;

/**
 * Looks for nested if/else condition blocks.
 */
public class NestedConditions extends AbstractIssueFinder {

    public static final String NAME = "nested_conditions";

    @Override
    public void visit(IfElseStmt node) {
        if (hasNested(node.getStmtList().getStmts()) || hasNested(node.getElseStmts().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (hasNested(node.getThenStmts().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    private boolean hasNested(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            if (stmt instanceof IfStmt) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
