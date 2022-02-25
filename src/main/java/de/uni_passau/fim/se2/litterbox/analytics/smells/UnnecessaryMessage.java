package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * If a message is sent in the second block of a script the receiving script could also start with the event of the first script.
 */
public class UnnecessaryMessage extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_message";
    private Set<Message> messagesInFirstPlace;
    private Set<Message> messagesInOtherPlace;
    private boolean searching;

    @Override
    public void visit(Program node) {
        messagesInOtherPlace = new LinkedHashSet<>();
        messagesInFirstPlace = new LinkedHashSet<>();
        searching = true;
        super.visit(node);
        searching = false;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        List<Stmt> stmts = node.getStmtList().getStmts();
        if (!stmts.isEmpty() && stmts.get(0) instanceof Broadcast) {
            Broadcast brd = (Broadcast) stmts.get(0);
            if (searching) {
                messagesInFirstPlace.add(brd.getMessage());
            } else {
                //messages that are sent in other parts of a scripts should not be counted
                if (!messagesInOtherPlace.contains(brd.getMessage())) {
                    currentScript = node;
                    currentProcedure = null;
                    addIssue(brd, brd.getMetadata());
                }
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(Broadcast node) {
        detectIfMessageInsideScript(node, node.getMessage());
    }

    @Override
    public void visit(BroadcastAndWait node) {
        detectIfMessageInsideScript(node, node.getMessage());
    }

    private void detectIfMessageInsideScript(ASTNode node, Message message) {
        if (searching) {
            ASTNode parentStmtList = node.getParentNode();
            StmtList stmtListNode = (StmtList) parentStmtList;
            ASTNode grandParentNode = stmtListNode.getParentNode();
            List<Stmt> stmts = stmtListNode.getStmts();
            int index = -1;
            for (int i = 0; i < stmts.size(); i++) {
                if (stmts.get(i) == node) {
                    index = i;
                    break;
                }
            }
            if (index > 0 || !(grandParentNode instanceof Script)) {
                messagesInOtherPlace.add(message);
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
