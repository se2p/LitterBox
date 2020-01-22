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
import scratch.ast.model.expression.ComparableExpr;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.num.DistanceTo;
import scratch.ast.model.expression.num.MouseX;
import scratch.ast.model.expression.num.MouseY;
import scratch.ast.model.expression.string.AttributeOf;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.control.UntilStmt;
import scratch.ast.opcodes.StringExprOpcode;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class PositionEqualsCheck implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are equals checks in conditions in your project.";
    private static final String NOTE2 = "Some of the conditions contain equals checks.";
    public static final String NAME = "equals_condition";
    public static final String SHORT_NAME = "eqCond";
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
    public void visit(WaitUntil node) {
        if (node.getUntil() instanceof Equals) {
            checkEquals((Equals) node.getUntil());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    private void checkEquals(Equals equals) {
        checkOptions(equals.getOperand1());
        checkOptions(equals.getOperand2());
    }

    private void checkOptions(ComparableExpr operand) {
        if (operand instanceof MouseX || operand instanceof MouseY || operand instanceof DistanceTo) {
            count++;
            found = true;
        } else if (operand instanceof AttributeOf) {
            if (((AttributeOf) operand).getAttribute().equals(new StringLiteral(StringExprOpcode.motion_xposition.name())) ||
                    ((AttributeOf) operand).getAttribute().equals(new StringLiteral(StringExprOpcode.motion_yposition.name()))) {
                count++;
                found = true;
            }
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (node.getBoolExpr() instanceof Equals) {
            checkEquals((Equals) node.getBoolExpr());
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
