package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Looks for nested if/else condition blocks.
 */
public class NestedConditionalChecks extends AbstractIssueFinder {

    public static final String NAME = "nested_conditional_checks";
    private List<ASTNode> addedStmts = new ArrayList<>();

    @Override
    public void visit(ActorDefinition actor) {
        addedStmts = new ArrayList<>();
        super.visit(actor);
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode())
                && (hasNested(node.getStmtList().getStmts()) || hasNested(node.getElseStmts().getStmts()))) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode()) && hasNested(node.getThenStmts().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
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
