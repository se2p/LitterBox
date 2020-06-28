/*
 * Copyright (C) 2020 LitterBox contributors
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

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A script should execute actions when an event occurs. Instead of continuously checking for the event to occur
 * inside a forever or until loop it is only checked once in a conditional construct, making it
 * unlikely that the timing is correct.
 */
public class MissingLoopSensing implements IssueFinder, ScratchVisitor {
    public static final String NAME = "missing_loop_sensing";
    public static final String SHORT_NAME = "mssLoopSens";
    public static final String HINT_TEXT = "missing loop sensing";
    private int count = 0;
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;
    private Set<Issue> issues = new LinkedHashSet<>();
    private ActorDefinition currentActor;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            insideGreenFlagClone = true;
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideGreenFlagClone = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            BoolExpr boolExpr = node.getBoolExpr();
            if (boolExpr instanceof IsKeyPressed || boolExpr instanceof Touching || boolExpr instanceof IsMouseDown || boolExpr instanceof ColorTouchingColor) {
                count++;
                issues.add(new Issue(this, currentActor, node));
                addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            BoolExpr boolExpr = node.getBoolExpr();
            if (boolExpr instanceof IsKeyPressed || boolExpr instanceof Touching || boolExpr instanceof IsMouseDown || boolExpr instanceof ColorTouchingColor) {
                count++;
                issues.add(new Issue(this, currentActor, node));
                addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
