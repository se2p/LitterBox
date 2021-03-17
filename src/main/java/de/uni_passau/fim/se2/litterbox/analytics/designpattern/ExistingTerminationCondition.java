package de.uni_passau.fim.se2.litterbox.analytics.designpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

public class ExistingTerminationCondition extends AbstractIssueFinder {
    public static final String NAME = "existing_termination";

    @Override
    public void visit(UntilStmt node) {
        if (!(node.getBoolExpr() instanceof UnspecifiedBoolExpr)) {
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
        return IssueType.SOLUTION;
    }
}
