/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.PositionEqualsCheck;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;

/**
 * It detects usage of Boolean Expressions in the project.
 */
public class BoolExpression extends AbstractIssueFinder {
    public static final String NAME = "boolean_expression";

    @Override
    public void visit(And expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(BiggerThan expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Equals expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(LessThan expr) {
        if (!(isLiteral(expr.getOperand1()) && isLiteral(expr.getOperand2()))) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Not expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    @Override
    public void visit(Or expr) {
        if (!(expr.getOperand1() instanceof UnspecifiedBoolExpr)
                && !(expr.getOperand2() instanceof UnspecifiedBoolExpr)) {
            addIssue(expr, expr.getMetadata(), IssueSeverity.LOW);
        }
        visitChildren(expr);
    }

    private boolean isLiteral(ASTNode node) {
        return (node instanceof NumberLiteral || node instanceof StringLiteral);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isSubsumedBy(Issue theIssue, Issue other) {
        if (theIssue.getFinder() != this) {
            return super.isSubsumedBy(theIssue, other);
        }

        if (other.getFinder() instanceof PositionEqualsCheck) {
            //if there is a PEQ than Litterbox shouldn't flag the same sport as a perfume of type BoolExpression
            return theIssue.getCodeLocation().equals(other.getCodeLocation());
        }

        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
