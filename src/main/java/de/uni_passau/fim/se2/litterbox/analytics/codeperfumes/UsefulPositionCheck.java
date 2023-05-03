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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * In an until or wait until loop the condition can include distances to other sprites or mouse positions. These values
 * are floating point values, therefore an equals comparison might never match exactly. For most cases a BiggerThan or
 * LessThan is needed when working with distances and positions. This is the solution pattern for "Position Equals
 * Check" bug.
 */
public class UsefulPositionCheck extends AbstractIssueFinder {
    public static final String NAME = "useful_position_check";
    private boolean inCondition;

    @Override
    public void visit(WaitUntil node) {
        inCondition = true;
        visitChildren(node);
        inCondition = false;
    }

    @Override
    public void visit(BiggerThan node) {
        if (inCondition) {
            if (containsCritical(node.getOperand1()) || containsCritical(node.getOperand2())) {
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
    }

    @Override
    public void visit(LessThan node) {
        if (inCondition) {
            if (containsCritical(node.getOperand1()) || containsCritical(node.getOperand2())) {
                addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
    }

    private boolean containsCritical(ComparableExpr operand) {
        if (operand instanceof MouseX || operand instanceof MouseY || operand instanceof DistanceTo
                || operand instanceof PositionX || operand instanceof PositionY) {
            return true;
        } else if (operand instanceof AttributeOf attributeOf) {
            if (attributeOf.getAttribute() instanceof AttributeFromFixed attributeFromFixed) {
                return attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.X_POSITION
                        || attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.Y_POSITION;
            }
        }
        return false;
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
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
