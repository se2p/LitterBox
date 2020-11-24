package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

/**
 * This finder looks for a forever loop that contains an if loop that stops at least the script.
 */
public class ComplicatedPositiveOneTime extends AbstractIssueFinder {
    public static final String NAME = "complicated_positive_one_time";
    private boolean insideForeverWithOneStmt;
    private boolean insideForeverAndIf;
    private boolean hasStop;

    @Override
    public void visit(RepeatForeverStmt node) {
        if (node.getStmtList().getStmts().size() == 1) {
            insideForeverWithOneStmt = true;
        }
        visitChildren(node);
        insideForeverWithOneStmt = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideForeverWithOneStmt) {
            insideForeverAndIf = true;
        }
        visitChildren(node);
        if (insideForeverWithOneStmt && hasStop) {
            addIssue(node, node.getMetadata());
            hasStop = false;
        }
        insideForeverAndIf = false;
    }

    @Override
    public void visit(TerminationStmt node) {
        if (insideForeverAndIf) {
            hasStop = true;
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
