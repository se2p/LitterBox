package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.List;

/**
 * Checks for an If-/IfElse block inside of a loop.
 */
public class NestedConditionInLoop extends AbstractIssueFinder {

    public static final String NAME = "nested_condition_in_loop";

    @Override
    public void visit(RepeatForeverStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    private void hasNested(List<Stmt> stmtList) {
        for (Stmt stmt : stmtList) {
            if (stmt instanceof IfStmt) {
                addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
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
