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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * A script should execute actions when an event occurs. Instead of continuously checking for the event to occur
 * inside a forever or until loop it is only checked once in a conditional construct, making it
 * unlikely that the timing is correct.
 */
public class MissingLoopSensing implements IssueFinder, ScratchVisitor {
    public static final String NAME = "missing_loop_sensing";
    public static final String SHORT_NAME = "mssLoopSens";
    private static final String NOTE1 = "There is no fishy touching or keyPressed checks without a loop.";
    private static final String NOTE2 = "The project contains some fishy touching and / or keyPressed checks without " +
            "a loop.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;

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
    public void visit(ActorDefinition actor) {
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
        if (found) {
            found = false;
            actorNames.add(actor.getIdent().getName());
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideGreenFlagClone = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            BoolExpr boolExpr = node.getBoolExpr();
            if (boolExpr instanceof IsKeyPressed || boolExpr instanceof Touching || boolExpr instanceof IsMouseDown || boolExpr instanceof ColorTouchingColor) {
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

    @Override
    public void visit(IfElseStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            BoolExpr boolExpr = node.getBoolExpr();
            if (boolExpr instanceof IsKeyPressed || boolExpr instanceof Touching || boolExpr instanceof IsMouseDown || boolExpr instanceof ColorTouchingColor) {
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

    @Override
    public String getName() {
        return NAME;
    }
}
