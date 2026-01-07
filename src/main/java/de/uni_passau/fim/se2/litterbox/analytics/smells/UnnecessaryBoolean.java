/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;

import java.util.Arrays;
import java.util.Collection;

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
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(equals, getBooleanExpression(equals.getOperand1()));
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                IssueBuilder builder = prepareIssueBuilder(equals)
                        .withHint(HINT_TRUE)
                        .withHintParameter("VALUE", getBooleanLiteral(equals.getOperand2()))
                        .withSeverity(IssueSeverity.HIGH)
                        .withRefactoring(refactoring);
                addIssue(builder);
            } else if (isBooleanFalseLiteral(equals.getOperand2())) {
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(equals, new Not(getBooleanExpression(equals.getOperand1()), equals.getMetadata()));
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                IssueBuilder builder = prepareIssueBuilder(equals)
                        .withHint(HINT_FALSE)
                        .withHintParameter("VALUE", getBooleanLiteral(equals.getOperand2()))
                        .withSeverity(IssueSeverity.HIGH)
                        .withRefactoring(refactoring);
                addIssue(builder);
            }
        } else if (isBooleanExpression(equals.getOperand2())) {
            if (isBooleanTrueLiteral(equals.getOperand1())) {
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(equals, getBooleanExpression(equals.getOperand2()));
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                IssueBuilder builder = prepareIssueBuilder(equals)
                        .withHint(HINT_TRUE)
                        .withHintParameter("VALUE", getBooleanLiteral(equals.getOperand1()))
                        .withSeverity(IssueSeverity.HIGH)
                        .withRefactoring(refactoring);
                addIssue(builder);
            } else if (isBooleanFalseLiteral(equals.getOperand1())) {
                NodeReplacementVisitor visitor = new NodeReplacementVisitor(equals, new Not(getBooleanExpression(equals.getOperand2()), equals.getMetadata()));
                ScriptEntity refactoring = visitor.apply(getCurrentScriptEntity());
                IssueBuilder builder = prepareIssueBuilder(equals)
                        .withHint(HINT_FALSE)
                        .withHintParameter("VALUE", getBooleanLiteral(equals.getOperand1()))
                        .withSeverity(IssueSeverity.HIGH)
                        .withRefactoring(refactoring);
                addIssue(builder);
            }
        }
    }

    private boolean isBooleanTrueLiteral(Expression expr) {
        if (expr instanceof StringLiteral literal) {
            return literal.getText().equalsIgnoreCase("true");
        } else if (expr instanceof NumberLiteral literal) {
            return ((int) literal.getValue()) == 1;
        } else if (expr instanceof AsNumber asNumber) {
            return isBooleanTrueLiteral(asNumber.getOperand1()); // Recursive check
        }

        return false;
    }

    private boolean isBooleanFalseLiteral(Expression expr) {
        if (expr instanceof StringLiteral literal) {
            return literal.getText().equalsIgnoreCase("false");
        } else if (expr instanceof NumberLiteral literal) {
            return ((int) literal.getValue()) == 0;
        } else if (expr instanceof AsNumber asNumber) {
            return isBooleanFalseLiteral(asNumber.getOperand1()); // Recursive check
        }

        return false;
    }

    private String getBooleanLiteral(Expression expr) {
        if (expr instanceof StringLiteral literal) {
            return literal.getText();
        } else if (expr instanceof NumberLiteral literal) {
            return Integer.toString((int) literal.getValue());
        } else if (expr instanceof AsNumber asNumber) {
            return getBooleanLiteral(asNumber.getOperand1()); // Recursive extraction
        }
        throw new RuntimeException("Unknown literal type: " + expr.getClass().getName());
    }

    private boolean isBooleanExpression(Expression expr) {
        if (expr instanceof AsString asString) {
            if (asString.getOperand1() instanceof BoolExpr) {
                return true;
            }
        }
        if (expr instanceof AsNumber asNumber) {
            return asNumber.getOperand1() instanceof BoolExpr;
        }
        return false;
    }

    private BoolExpr getBooleanExpression(Expression expr) {
        if (expr instanceof AsString asString) {
            return (BoolExpr) asString.getOperand1();
        }
        if (expr instanceof AsNumber asNumber) {
            return (BoolExpr) asNumber.getOperand1();
        }
        throw new IllegalArgumentException("Unknown boolean expression type: " + expr.getClass().getName());
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
