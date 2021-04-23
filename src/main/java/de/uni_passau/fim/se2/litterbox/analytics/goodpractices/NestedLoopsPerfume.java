package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for (not empty or useless) nested loops in the project.
 */
public class NestedLoopsPerfume extends AbstractIssueFinder {
    public static final String NAME = "nested_loops_perfume";
    private List<ASTNode> addedStmts = new ArrayList<>();

    @Override
    public void visit(RepeatForeverStmt node) {
        // If the outer loop is already added as NestedLoop Issue, then ignore the rest
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
        if (stmtList.size() > 1) {
            for (Stmt stmt : stmtList) {
                if (stmt instanceof ControlStmt) {
                    return true;
                }
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
