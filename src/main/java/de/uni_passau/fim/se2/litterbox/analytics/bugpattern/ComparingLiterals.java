/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * Reporter blocks are used to evaluate the truth value of certain expressions.
 * Not only is it possible to compare literals to variables or the results of other reporter blocks, literals can
 * also be compared to literals.
 * Since this will lead to the same result in each execution this construct is unnecessary and can obscure the fact
 * that certain blocks will never or always be executed.
 */
public class ComparingLiterals extends AbstractIssueFinder {

    public static final String NAME = "comparing_literals";
    public static final String HINT_TRUE_FALSE = "TRUEFALSE";

    @Override
    public void visit(Equals node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint = new Hint(getName());
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(text1 == text2)));
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                if (result == 0) {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("true"));
                } else {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("false"));
                }
            }
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    private String getLiteralValue(ComparableExpr node) {
        String text1;
        if (node instanceof StringLiteral) {
            text1 = ((StringLiteral) node).getText();
        } else {
            text1 = String.valueOf(((NumberLiteral) node).getValue());
        }
        return text1;
    }

    @Override
    public void visit(LessThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint = new Hint(getName());
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(text1 < text2)));
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                if (result < 0) {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("true"));
                } else {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("false"));
                }
            }
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint = new Hint(getName());
            if (node.getOperand1() instanceof NumberLiteral && node.getOperand2() instanceof NumberLiteral) {
                double text1 = ((NumberLiteral) node.getOperand1()).getValue();
                double text2 = ((NumberLiteral) node.getOperand2()).getValue();
                hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo(String.valueOf(text1 > text2)));
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                if (result > 0) {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("true"));
                } else {
                    hint.setParameter(HINT_TRUE_FALSE, IssueTranslator.getInstance().getInfo("false"));
                }
            }
            addIssue(node, node.getMetadata(), hint);
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
