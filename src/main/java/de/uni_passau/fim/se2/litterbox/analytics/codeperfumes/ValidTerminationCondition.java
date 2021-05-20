package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * The repeat until blocks require a valid stopping condition to work as intended. This is the solution pattern for
 * the bug pattern "Missing Termination Condition"
 */
public class ValidTerminationCondition extends AbstractIssueFinder {
    public static final String NAME = "valid_termination";

    @Override
    public void visit(UntilStmt node) {
        if (!(node.getBoolExpr() instanceof UnspecifiedBoolExpr)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
