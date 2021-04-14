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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;

/**
 * This happens when inside a block that expects a colour or sprite as parameter (e.g., set pen color to or
 * touching mouse-pointer?) a reporter block, or an expression with a string or number value is used.
 */
public class ExpressionAsTouchingOrColor extends AbstractIssueFinder implements PenExtensionVisitor {
    public static final String NAME = "expression_as_touching_or_color";

    @Override
    public void visit(PenStmt node) {
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (!(node.getColorExpr() instanceof ColorLiteral)) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (!(node.getOperand1() instanceof ColorLiteral)) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
        // TODO: Should this be an else-if rather than if, to avoid duplicate reports?
        if (!(node.getOperand2() instanceof ColorLiteral)) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (!(node.getColor() instanceof ColorLiteral)) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Touching node) {
        if (!(node.getTouchable() instanceof MousePointer)
                && !(node.getTouchable() instanceof Edge)
                && !(node.getTouchable() instanceof SpriteTouchable)) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
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
