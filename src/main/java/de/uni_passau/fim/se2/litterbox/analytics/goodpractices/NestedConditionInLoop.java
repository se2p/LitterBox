package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for an If-/IfElse block inside of a loop.
 */
public class NestedConditionInLoop extends AbstractIssueFinder {

    public static final String NAME = "nested_condition_in_loop";
    private List<ASTNode> addedStmts = new ArrayList<>();

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode()) && hasNested(node.getStmtList().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode()) && hasNested(node.getStmtList().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode()) && hasNested(node.getStmtList().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    private boolean hasNested(List<Stmt> stmtList) {
        for (Stmt stmt : stmtList) {
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
