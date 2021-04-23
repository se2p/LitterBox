package de.uni_passau.fim.se2.litterbox.analytics.goodpractices;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

/**
 * If an initialized variable is changed repeatedly (in a RepeatForever) after
 * a WaitSeconds block, it is seen as a counter.
 */
public class Counter extends AbstractIssueFinder {

    public static final String NAME = "counter";
    private boolean insideLoop = false;
    private boolean waitSec = false;
    private String counterVariable = null;

    @Override
    public void visit(SetVariableTo node) {
        if (node.getIdentifier() instanceof Qualified) {
            counterVariable = ((Qualified) node.getIdentifier()).getSecond().getName().getName();
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (counterVariable != null) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(StmtList node) {
        if (insideLoop && counterVariable != null) {
            node.getStmts().forEach(stmt -> {
                if (stmt instanceof WaitSeconds) {
                    waitSec = true;
                } else if (stmt instanceof ChangeVariableBy) {
                    if (waitSec) {
                        if (((ChangeVariableBy) stmt).getIdentifier() instanceof Qualified) {
                            String changedVariable = ((Qualified) ((ChangeVariableBy) stmt).
                                    getIdentifier()).getSecond().getName().getName();
                            if (changedVariable.equals(counterVariable)) {
                                addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
                                waitSec = false;
                            }
                        }
                    }
                }
            });
        }
        visitChildren(node);
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.GOOD_PRACTICE;
    }
}
