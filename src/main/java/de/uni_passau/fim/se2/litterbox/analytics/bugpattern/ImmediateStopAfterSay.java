package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmediateStopAfterSay extends AbstractIssueFinder {
    public static final String NAME = "immediate_stop_after_say_think";
    public static final String THINK_HINT = "immediate_stop_after_think";
    public static final String SAY_HINT = "immediate_stop_after_say";

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        if (stmts.size() > 1 && stmts.get(stmts.size() - 1) instanceof StopAll) {
            ASTNode questionableNode = stmts.get(stmts.size() - 2);
            if (questionableNode instanceof Say) {
                addIssue(questionableNode, questionableNode.getMetadata(), new Hint(SAY_HINT));
            } else if (questionableNode instanceof Think) {
                addIssue(questionableNode, questionableNode.getMetadata(), new Hint(THINK_HINT));
            }
        }
        super.visitChildren(node);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(THINK_HINT);
        keys.add(SAY_HINT);
        return keys;
    }
}
