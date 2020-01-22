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

import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.expression.bool.BiggerThan;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.bool.LessThan;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class ComparingLiterals implements IssueFinder, ScratchVisitor {

    public static final String NAME = "comparing_literals";
    public static final String SHORT_NAME = "compLit";

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
        return new IssueReport(NAME, count, actorNames, "");
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
    public void visit(Equals node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
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
    public void visit(LessThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
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
    public void visit(BiggerThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            count++;
            found = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
