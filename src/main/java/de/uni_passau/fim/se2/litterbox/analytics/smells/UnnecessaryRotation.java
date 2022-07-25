package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;

public class UnnecessaryRotation extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_rotation";

    @Override
    public void visit(TurnLeft node) {
        if (hasUnnecessaryValue(node.getDegrees())) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (hasUnnecessaryValue(node.getDegrees())) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    private boolean hasUnnecessaryValue(NumExpr degrees) {
        if (degrees instanceof NumberLiteral) {
            double value = ((NumberLiteral) degrees).getValue();
            return value % 360 == 0;
        }
        return false;
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
