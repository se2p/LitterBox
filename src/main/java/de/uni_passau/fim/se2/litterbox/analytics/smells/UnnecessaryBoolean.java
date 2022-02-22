package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Comparing a boolean with 1/0 true/false is the same as just using the boolean directly.
 */
public class UnnecessaryBoolean extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_boolean";

    public static final String HINT_TRUE = "unnecessary_boolean_true";
    public static final String HINT_FALSE = "unnecessary_boolean_false";

    @Override
    public void visit(Equals equals) {
        if (isBooleanExpression(equals.getOperand1())) {
            if (isBooleanTrueLiteral(equals.getOperand2())) {
                Hint hint = new Hint(HINT_TRUE);
                hint.setParameter("VALUE", getBooleanLiteral(equals.getOperand2()));
                addIssue(equals, equals.getMetadata(), hint);
            } else if (isBooleanFalseLiteral(equals.getOperand2())) {
                Hint hint = new Hint(HINT_FALSE);
                hint.setParameter("VALUE", getBooleanLiteral(equals.getOperand2()));
                addIssue(equals, equals.getMetadata(), hint);
            }
        } else if (isBooleanExpression(equals.getOperand2())) {
            if (isBooleanTrueLiteral(equals.getOperand1())) {
                Hint hint = new Hint(HINT_TRUE);
                hint.setParameter("VALUE", getBooleanLiteral(equals.getOperand1()));
                addIssue(equals, equals.getMetadata(), hint);
            } else if (isBooleanFalseLiteral(equals.getOperand1())) {
                Hint hint = new Hint(HINT_FALSE);
                hint.setParameter("VALUE", getBooleanLiteral(equals.getOperand1()));
                addIssue(equals, equals.getMetadata(), hint);
            }
        }
    }

    private boolean isBooleanTrueLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral) {
            StringLiteral literal = (StringLiteral) expr;
            if (literal.getText().equalsIgnoreCase("true")) {
                return true;
            }
        } else if (expr instanceof NumberLiteral) {
            NumberLiteral literal = (NumberLiteral) expr;
            if (((int) literal.getValue()) == 1) {
                return true;
            }
        }

        return false;
    }

    private boolean isBooleanFalseLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral) {
            StringLiteral literal = (StringLiteral) expr;
            if (literal.getText().equalsIgnoreCase("false")) {
                return true;
            }
        } else if (expr instanceof NumberLiteral) {
            NumberLiteral literal = (NumberLiteral) expr;
            if (((int) literal.getValue()) == 0) {
                return true;
            }
        }

        return false;
    }

    private String getBooleanLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral) {
            StringLiteral literal = (StringLiteral) expr;
            return literal.getText();
        } else if (expr instanceof NumberLiteral) {
            NumberLiteral literal = (NumberLiteral) expr;
            return Integer.toString((int) literal.getValue());
        }
        throw new RuntimeException("Unknown literal type");
    }

    private boolean isBooleanExpression(ComparableExpr expr) {
        if (expr instanceof AsString) {
            AsString asString = (AsString) expr;
            if (asString.getOperand1() instanceof BoolExpr) {
                return true;
            }
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

    @Override
    public Collection<String> getHintKeys() {
        return Arrays.asList(HINT_TRUE, HINT_FALSE);
    }
}
