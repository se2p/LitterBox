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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.AsExprType;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This smell detects empty parameters.
 */
public class EmptyParameter extends AbstractIssueFinder {
    private static final String NAME = "empty_parameter";
    private static final String HINT = "empty_parameter";
    private static final String HINT_DIRECTION = "empty_parameter_direction";
    private static final String HINT_COLOR = "empty_parameter_color";
    private static final String HINT_POSITION = "empty_parameter_position";

    /**
     * Stores nodes that have already been handled by a specific visitor method (e.g., visit(SetXTo)).
     * <p>
     * When a specific visitor detects and reports an empty parameter, it adds the node to this set.
     * We continue traversal (super.visit) to ensure we find nested issues in other parameters.
     * When the generic visit(StringLiteral) is reached, it checks this set to avoid reporting
     * the same issue twice.
     */
    private Set<ASTNode> handledNodes;

    @Override
    public Set<Issue> check(Program program) {
        handledNodes = new HashSet<>();
        return super.check(program);
    }

    @Override
    public void visit(PointInDirection node) {
        checkAndReport(node.getDirection(), HINT_DIRECTION);
        super.visit(node);
    }

    @Override
    public void visit(PenBlock node) {
        if (node instanceof SetPenColorToColorStmt) {
            checkAndReport(((SetPenColorToColorStmt) node).getColorExpr(), HINT_COLOR);
        }
        super.visit(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkAndReport(node.getColor(), HINT_COLOR);
        super.visit(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkAndReport(node.getOperand1(), HINT_COLOR);
        checkAndReport(node.getOperand2(), HINT_COLOR);
        super.visit(node);
    }

    @Override
    public void visit(GoToPos node) {
        checkAndReport(node.getPosition(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(GoToPosXY node) {
        checkAndReport(node.getX(), HINT_POSITION);
        checkAndReport(node.getY(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        checkAndReport(node.getNum(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        checkAndReport(node.getNum(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(GlideSecsTo node) {
        checkAndReport(node.getSecs(), HINT_POSITION);
        checkAndReport(node.getPosition(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(GlideSecsToXY node) {
        checkAndReport(node.getSecs(), HINT_POSITION);
        checkAndReport(node.getX(), HINT_POSITION);
        checkAndReport(node.getY(), HINT_POSITION);
        super.visit(node);
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (handledNodes.contains(stringLiteral)) {
            return;
        }

        if (stringLiteral.getText().isEmpty()) {
            if (getCurrentScriptEntity() == null) {
                return;
            }

            ASTNode parent = stringLiteral.getParentNode();

            boolean isSayOrThink = parent instanceof Say || parent instanceof SayForSecs
                    || parent instanceof Think || parent instanceof ThinkForSecs || parent instanceof AskAndWait;
            if (!isSayOrThink) {
                addIssue(prepareIssueBuilder(stringLiteral, HINT).withSeverity(IssueSeverity.HIGH));
            }
        }
        super.visit(stringLiteral);
    }

    private void checkAndReport(ASTNode node, String hintKey) {
        ASTNode emptyNode = getEmptyParameterNode(node);
        if (emptyNode != null) {
            addIssue(prepareIssueBuilder(emptyNode, hintKey).withSeverity(IssueSeverity.HIGH));
            handledNodes.add(emptyNode);
        }
    }

    private ASTNode getEmptyParameterNode(ASTNode node) {
        if (node instanceof StringLiteral literal) {
            if (literal.getText().isEmpty()) {
                return literal;
            }
        } else if (node instanceof UnspecifiedExpression || node instanceof UnspecifiedNumExpr) {
            return node;
        } else if (node instanceof AsExprType || node instanceof LocalIdentifier
                || node instanceof Variable || node instanceof Qualified
                || node instanceof FromExpression || node instanceof FromNumber) {
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    ASTNode res = getEmptyParameterNode(child);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void visit(UnspecifiedExpression unspecifiedExpression) {
        if (handledNodes.contains(unspecifiedExpression)) {
            return;
        }
        // Unspecified expressions are always empty parameters
        addIssue(prepareIssueBuilder(unspecifiedExpression, HINT).withSeverity(IssueSeverity.HIGH));
        super.visit(unspecifiedExpression);
    }

    @Override
    public void visit(UnspecifiedNumExpr unspecifiedNumExpr) {
        if (handledNodes.contains(unspecifiedNumExpr)) {
            return;
        }
        // Unspecified number expressions are always empty parameters
        addIssue(prepareIssueBuilder(unspecifiedNumExpr, HINT).withSeverity(IssueSeverity.HIGH));
        super.visit(unspecifiedNumExpr);
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
    public List<String> getHintKeys() {
        return Arrays.asList(HINT, HINT_DIRECTION, HINT_COLOR, HINT_POSITION);
    }

    protected IssueBuilder prepareIssueBuilder(ASTNode node) {
        return super.prepareIssueBuilder(node).withHint(HINT);
    }

    protected IssueBuilder prepareIssueBuilder(ASTNode node, String hintKey) {
        return super.prepareIssueBuilder(node).withHint(hintKey);
    }
}
