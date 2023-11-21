package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

public class MissingLoopSensingLoopFix extends AbstractIssueFinder {
    public static final String NAME = "missing_loop_sensing_fix";
    private final String bugLocationBlockId;

    public MissingLoopSensingLoopFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    public void visit(ASTNode node) {
        if (AstNodeUtil.getBlockId(node).equals(bugLocationBlockId)) {
            StmtList stmtList = AstNodeUtil.findParent(node, StmtList.class);
            assert stmtList != null;
            if (stmtList.getParentNode() instanceof UntilStmt || stmtList.getParentNode() instanceof RepeatForeverStmt) {
                addIssue(node, node.getMetadata());
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
