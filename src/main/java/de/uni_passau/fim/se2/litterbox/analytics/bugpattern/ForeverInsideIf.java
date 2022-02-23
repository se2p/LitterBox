package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

/**
 * If a forever loop is nested inside an if statement, the inner loop will never terminate. Thus
 * the statements following the if statement can never be reached.
 */
public class ForeverInsideIf extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_if";
    private int ifFollowingCounter;

    @Override
    public void visit(Script node) {
        ifFollowingCounter = 0;
        super.visit(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        ifFollowingCounter = 0;
        super.visit(node);
    }

    @Override
    public void visit(StmtList node) {
        boolean hasIf = false;
        //size - 1 so that there are following blocks
        for (int i = 0; i < node.getStmts().size() - 1; i++) {
            if (node.getStmts().get(0) instanceof IfStmt) {
                ifFollowingCounter++;
                hasIf = true;
                break;
            }
        }
        super.visit(node);
        if (hasIf) {
            ifFollowingCounter--;
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (ifFollowingCounter > 0) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
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
