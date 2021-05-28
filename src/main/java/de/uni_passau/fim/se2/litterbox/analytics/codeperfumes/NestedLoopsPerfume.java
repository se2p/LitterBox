package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
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
    public void visit(ActorDefinition actor) {
        addedStmts = new ArrayList<>();
        super.visit(actor);
    }

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
                if (stmt instanceof RepeatForeverStmt || stmt instanceof RepeatTimesStmt || stmt instanceof UntilStmt) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
