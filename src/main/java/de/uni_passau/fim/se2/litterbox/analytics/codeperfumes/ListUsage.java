package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;

/**
 * List as the only collection in Scratch is important to understand and to manipulate it is a sign for good
 * computational understanding.
 */
public class ListUsage extends AbstractIssueFinder {

    public static final String NAME = "list_usage";


    public void visit(AddTo node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(DeleteAllOf node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(DeleteOf node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(InsertAt node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(ReplaceItem node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(HideList node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    public void visit(ShowList node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
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
