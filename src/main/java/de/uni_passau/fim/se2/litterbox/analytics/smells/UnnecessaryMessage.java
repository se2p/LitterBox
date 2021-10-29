package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.List;
import java.util.Set;

/**
 * If a message is sent in the second block of a script the receiving script could also start with the event of the first script.
 */
public class UnnecessaryMessage extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_message";
    public Set<Message> messages;

    @Override
    public void visit(Script node){
        List<Stmt> stmts = node.getStmtList().getStmts();
        if(stmts.get(0) instanceof Broadcast){
            Broadcast brd = (Broadcast) stmts.get(0);
            brd.getMessage();
        }else if(stmts.get(0) instanceof BroadcastAndWait){

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
