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
package analytics.bugpattern;

import java.util.LinkedList;
import java.util.List;
import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.expression.bool.ColorTouches;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.literals.ColorLiteral;
import scratch.ast.model.statement.pen.SetPenColorToColorStmt;
import scratch.ast.model.touchable.Edge;
import scratch.ast.model.touchable.MousePointer;
import scratch.ast.model.touchable.SpriteTouchable;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class ExpressionAsColor implements IssueFinder, ScratchVisitor {
    public static final String NAME = "expression_as_color";
    public static final String SHORT_NAME = "exprColor";
    private static final String NOTE1 = "There are no expressions used as colors in your project.";
    private static final String NOTE2 = "Some of the sprites use expressions as colors.";
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
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(ColorTouches node) {
        if (!(node.getOperand1() instanceof ColorLiteral)) {
            count++;
            found = true;
        }
        if (!(node.getOperand2() instanceof ColorLiteral)) {
            count++;
            found = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Touching node) {
        if (!(node.getTouchable() instanceof MousePointer) && !(node.getTouchable() instanceof Edge) && !(node.getTouchable() instanceof SpriteTouchable)) {
            if (!(node.getTouchable() instanceof ColorLiteral)) {
                count++;
                found = true;
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
