package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class UnnecessaryProcedure extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_procedure";

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;
        if (node.getStmtList().getStmts().size() == 1) {
            addIssue(node, node.getMetadata().getDefinition(), IssueSeverity.LOW);
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
