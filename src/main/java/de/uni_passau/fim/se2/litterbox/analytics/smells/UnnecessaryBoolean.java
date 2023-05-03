/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
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

    private boolean isBooleanTrueLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral literal) {
            if (literal.getText().equalsIgnoreCase("true")) {
                return true;
            }
        } else if (expr instanceof NumberLiteral literal) {
            if (((int) literal.getValue()) == 1) {
                return true;
            }
        }

        return false;
    }

    private boolean isBooleanFalseLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral literal) {
            if (literal.getText().equalsIgnoreCase("false")) {
                return true;
            }
        } else if (expr instanceof NumberLiteral literal) {
            if (((int) literal.getValue()) == 0) {
                return true;
            }
        }

        return false;
    }

    private String getBooleanLiteral(ComparableExpr expr) {
        if (expr instanceof StringLiteral literal) {
            return literal.getText();
        } else if (expr instanceof NumberLiteral literal) {
            return Integer.toString((int) literal.getValue());
        }
        throw new RuntimeException("Unknown literal type");
    }

    private boolean isBooleanExpression(ComparableExpr expr) {
        if (expr instanceof AsString asString) {
            if (asString.getOperand1() instanceof BoolExpr) {
                return true;
            }
        }
        return false;
    }

    private BoolExpr getBooleanExpression(ComparableExpr expr) {
        return (BoolExpr) ((AsString) expr).getOperand1();
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
