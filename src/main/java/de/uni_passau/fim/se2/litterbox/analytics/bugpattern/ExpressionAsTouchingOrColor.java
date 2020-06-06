/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;

/**
 * This happens when inside a block that expects a colour or sprite as parameter (e.g., set pen color to or
 * touching mouse-pointer?) a reporter block, or an expression with a string or number value is used.
 */
public class ExpressionAsTouchingOrColor implements IssueFinder, ScratchVisitor {
    public static final String NAME = "expression_as_touching_or_color";
    public static final String SHORT_NAME = "exprTouchColor";
    public static final String HINT_TEXT = "expression as touching or color";
    private static final String NOTE1 = "There are no expressions used as touching or colors in your project.";
    private static final String NOTE2 = "Some of the sprites use expressions as touching or colors.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (!(node.getColorExpr() instanceof ColorLiteral)) {
            count++;
            found = true;
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (!(node.getOperand1() instanceof ColorLiteral)) {
            count++;
            found = true;
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }
        if (!(node.getOperand2() instanceof ColorLiteral)) {
            count++;
            found = true;
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (!(node.getColor() instanceof ColorLiteral)) {
            count++;
            found = true;
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Touching node) {
        if (!(node.getTouchable() instanceof MousePointer)
                && !(node.getTouchable() instanceof Edge)
                && !(node.getTouchable() instanceof SpriteTouchable)) {
            count++;
            found = true;
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }
    }
}
