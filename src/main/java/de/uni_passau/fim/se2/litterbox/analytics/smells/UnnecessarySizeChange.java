package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;

public class UnnecessarySizeChange extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_size_change";
    private static final int MAX_SIZE = 540;

    @Override
    public void visit(SetSizeTo node) {
        NumExpr expr = node.getPercent();
        if (expr instanceof NumberLiteral) {
            double value = ((NumberLiteral) expr).getValue();
            if (value <= 0 || value >= MAX_SIZE) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
            }
        }
    }

    @Override
    public void visit(ChangeSizeBy node) {
        NumExpr expr = node.getNum();
        if (expr instanceof NumberLiteral) {
            double value = ((NumberLiteral) expr).getValue();
            if (value == 0 || value >= MAX_SIZE || value <= -MAX_SIZE) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
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
