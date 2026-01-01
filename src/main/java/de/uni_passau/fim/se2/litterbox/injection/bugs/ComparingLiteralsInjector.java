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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.ast.builder.ComparisonBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;

/**
 * Injects the {@link ComparingLiterals} bug.
 *
 * <p>
 * <b>Bug Pattern:</b> A comparison where both operands are literals is likely a mistake,
 * since the result is always constant.
 *
 * <p>
 * <b>Injection Strategy:</b> Finds comparisons where one side is a literal and the other is not,
 * then replaces the non-literal with a matching literal type.
 */
public class ComparingLiteralsInjector extends AbstractComparisonInjector {

    private final IssueFinder issueFinder = new ComparingLiterals();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    protected void injectIntoEquals(Equals node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand2(matchingLiteral)
                    .buildEquals());
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand1(matchingLiteral)
                    .buildEquals());
        }
    }

    @Override
    protected void injectIntoLessThan(LessThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand2(matchingLiteral)
                    .buildLessThan());
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand1(matchingLiteral)
                    .buildLessThan());
        }
    }

    @Override
    protected void injectIntoBiggerThan(BiggerThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand2(matchingLiteral)
                    .buildBiggerThan());
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceNodeImmutably(node, n -> new ComparisonBuilder()
                    .from(n)
                    .withOperand1(matchingLiteral)
                    .buildBiggerThan());
        }
    }
}
