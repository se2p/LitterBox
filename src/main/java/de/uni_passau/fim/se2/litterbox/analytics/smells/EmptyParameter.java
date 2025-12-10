/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorParamTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * This smell detects empty parameters.
 */
public class EmptyParameter extends AbstractIssueFinder {
    private static final String NAME = "empty_parameter";
    private static final String HINT = "empty_parameter";

    private final Deque<ASTNode> parentStack = new ArrayDeque<>();

    @Override
    public void visitChildren(ASTNode node) {
        parentStack.push(node);
        super.visitChildren(node);
        parentStack.pop();
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (stringLiteral.getText().isEmpty()) {
            if (getCurrentScriptEntity() == null) {
                return;
            }

            ASTNode parent = parentStack.peek();
            
            boolean isSayOrThink = parent instanceof Say || parent instanceof SayForSecs
                    || parent instanceof Think || parent instanceof ThinkForSecs;
            if (!isSayOrThink) {
                String hintKey = getHintKeyFromStack();
                addIssue(prepareIssueBuilder(stringLiteral, hintKey).withSeverity(IssueSeverity.HIGH));
            }
        }
        super.visit(stringLiteral);
    }

    private String getHintKeyFromStack() {
        for (ASTNode node : parentStack) {
            if (node instanceof PointInDirection) {
                return "empty_parameter_direction";
            } else if (node instanceof SetPenColorToColorStmt
                    || node instanceof SetPenColorParamTo) {
                return "empty_parameter_color";
            } else if (node instanceof GoToPos
                    || node instanceof GoToPosXY) {
                return "empty_parameter_position";
            }
        }
        return HINT;
    }

    @Override
    public void visit(UnspecifiedExpression unspecifiedExpression) {
        String hintKey = getHintKeyFromStack();
        addIssue(prepareIssueBuilder(unspecifiedExpression, hintKey).withSeverity(IssueSeverity.HIGH));
        super.visit(unspecifiedExpression);
    }

    @Override
    public void visit(UnspecifiedNumExpr unspecifiedNumExpr) {
        String hintKey = getHintKeyFromStack();
        addIssue(prepareIssueBuilder(unspecifiedNumExpr, hintKey).withSeverity(IssueSeverity.HIGH));
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
        return Arrays.asList(HINT, "empty_parameter_direction", "empty_parameter_color", "empty_parameter_position");
    }

    protected IssueBuilder prepareIssueBuilder(ASTNode node) {
        return super.prepareIssueBuilder(node).withHint(HINT);
    }

    protected IssueBuilder prepareIssueBuilder(ASTNode node, String hintKey) {
        return super.prepareIssueBuilder(node).withHint(hintKey);
    }
}
