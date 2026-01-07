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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the FloatingPointComparison bug.
 *
 * <p>The bug is reported when equality comparison with floating-point values is used in conditions.
 * The bug is fixed if the comparison is no longer in a condition or uses safe operands.
 */
public class FloatingPointComparisonFix extends AbstractFixHeuristic {

    private boolean inCondition;

    private final FloatingPointComparison relatedBugFinder = new FloatingPointComparison();

    public FloatingPointComparisonFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        inCondition = false;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(WaitUntil node) {
        inCondition = true;
        visitChildren(node);
        inCondition = false;
    }

    @Override
    public void visit(UntilStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        node.getStmtList().accept(this);
    }

    @Override
    public void visit(IfThenStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        node.getThenStmts().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(Equals node) {
        if (matchesBugLocation(node)) {
            // Fixed if not in condition OR operands are safe
            if (!inCondition || checkEquals(node)) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(LessThan node) {
        if (matchesBugLocation(node)) {
            // LessThan is safe regarding floating point equality issues
            addFix(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (matchesBugLocation(node)) {
            // BiggerThan is safe regarding floating point equality issues
            addFix(node, node.getMetadata());
        }
        visitChildren(node);
    }

    private boolean checkEquals(Equals equals) {
        return isSafeOperand(equals.getOperand1()) && isSafeOperand(equals.getOperand2());
    }

    private boolean isSafeOperand(ComparableExpr operand) {
        if (operand instanceof MouseX || operand instanceof MouseY || operand instanceof DistanceTo
                || operand instanceof PositionX || operand instanceof PositionY
                || operand instanceof Direction || operand instanceof Volume || operand instanceof Timer) {
            return false;
        } else if (operand instanceof AttributeOf attributeOf
                && attributeOf.getAttribute() instanceof AttributeFromFixed attributeFromFixed) {
            FixedAttribute.FixedAttributeType type = attributeFromFixed.getAttribute().getType();
            return type != FixedAttribute.FixedAttributeType.X_POSITION
                    && type != FixedAttribute.FixedAttributeType.Y_POSITION
                    && type != FixedAttribute.FixedAttributeType.DIRECTION
                    && type != FixedAttribute.FixedAttributeType.VOLUME;
        }
        return true;
    }
}
