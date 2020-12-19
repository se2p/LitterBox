package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;

import java.util.List;

public class ImmediateDeleteCloneAfterBroadcast extends AbstractIssueFinder {
    private String NAME = "immediate_delete_clone_after_broadcast";

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        // check size > 1 because there has to be room for a say/think AND a stop stmt
        if (stmts.size() > 1 && stmts.get(stmts.size() - 1) instanceof DeleteClone) {
            ASTNode questionableNode = stmts.get(stmts.size() - 2);
            if (questionableNode instanceof Broadcast) {
                addIssue(questionableNode, questionableNode.getMetadata());
            }
        }
        super.visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
