package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;

public class UnnecessaryMove extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_move";

    @Override
    public void visit(MoveSteps node) {
        if (hasUnnecessaryValue(node.getSteps())) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (hasUnnecessaryValue(node.getNum())) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (hasUnnecessaryValue(node.getNum())) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    private boolean hasUnnecessaryValue(NumExpr degrees) {
        if (degrees instanceof NumberLiteral) {
            double value = ((NumberLiteral) degrees).getValue();
            return value == 0;
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
