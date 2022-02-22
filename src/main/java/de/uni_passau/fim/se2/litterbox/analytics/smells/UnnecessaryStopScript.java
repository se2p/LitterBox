package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

import java.util.List;

/**
 * A StopThisScript block at the end of a script is unnecessary
 */
public class UnnecessaryStopScript extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_stop_script";

    @Override
    public void visit(Script script) {
        if (ignoreLooseBlocks && script.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }

        List<Stmt> stmts = script.getStmtList().getStmts();
        if (!stmts.isEmpty()) {
            currentScript = script;
            Stmt last = stmts.get(stmts.size() - 1);
            if (last instanceof StopThisScript) {
                addIssue(last, last.getMetadata());
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
