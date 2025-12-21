/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the {@link ComparingLiterals} bug.
 * <p>
 * <b>Bug Pattern:</b> A comparison where both operands are literals is likely a mistake,
 * since the result is always constant.
 * <p>
 * <b>Injection Strategy:</b> Finds comparisons where one side is a literal and the other is not,
 * then replaces the non-literal with a matching literal type.
 */
public class ComparingLiteralsInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new ComparingLiterals();
    }

    @Override
    public void visit(Equals node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceWith(node, n -> new Equals(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceWith(node, n -> new Equals(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceWith(node, n -> new LessThan(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceWith(node, n -> new LessThan(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        if (isLiteral(op1) && !isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op1);
            replaceWith(node, n -> new BiggerThan(n.getOperand1(), matchingLiteral, n.getMetadata()));
        } else if (!isLiteral(op1) && isLiteral(op2)) {
            ComparableExpr matchingLiteral = createMatchingLiteral(op2);
            replaceWith(node, n -> new BiggerThan(matchingLiteral, n.getOperand2(), n.getMetadata()));
        }
        super.visit(node);
    }

    private boolean isLiteral(ComparableExpr expr) {
        return expr instanceof NumberLiteral || expr instanceof StringLiteral;
    }

    private ComparableExpr createMatchingLiteral(ComparableExpr reference) {
        if (reference instanceof NumberLiteral num) {
            return new NumberLiteral(num.getValue());
        } else if (reference instanceof StringLiteral str) {
            return new StringLiteral(str.getText());
        }
        return new NumberLiteral(0);
    }
}
