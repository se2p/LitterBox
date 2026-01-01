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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ExpressionAsTouchingOrColor;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

/**
 * Fix heuristic for the ExpressionAsTouchingOrColor bug.
 *
 * <p>The bug is reported when an expression slot is filled with a value rather than a reporter.
 * The bug is fixed if the expression is now a proper value (literal, sprite, etc).
 */
public class ExpressionAsTouchingOrColorFix extends AbstractFixHeuristic implements PenExtensionVisitor {

    private final ExpressionAsTouchingOrColor relatedBugFinder = new ExpressionAsTouchingOrColor();

    public ExpressionAsTouchingOrColorFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(ColorTouchingColor node) {
        if (matchesBugLocation(node)) {
            if (node.getOperand1() instanceof ColorLiteral && node.getOperand2() instanceof ColorLiteral) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (matchesBugLocation(node)) {
            if (node.getColor() instanceof ColorLiteral) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(Touching node) {
        if (matchesBugLocation(node)) {
            if (node.getTouchable() instanceof MousePointer
                    || node.getTouchable() instanceof Edge
                    || node.getTouchable() instanceof SpriteTouchable) {
                addFix(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (matchesBugLocation(node)) {
            if (node.getColorExpr() instanceof ColorLiteral) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}
