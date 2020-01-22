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
package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.event.KeyPressed;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.spritemotion.ChangeXBy;
import scratch.ast.model.statement.spritemotion.ChangeYBy;
import scratch.ast.model.statement.spritemotion.MoveSteps;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Checks for missing for-loops in movement scripts.
 */
public class StutteringMovement implements IssueFinder, ScratchVisitor {

    private static final String NOTE1 = "There are no scripts causing stuttering movement in your project.";
    private static final String NOTE2 = "There are some scripts causing stuttering movement in your project.";
    public static final String NAME = "stuttering_movement";
    public static final String SHORT_NAME = "stuttMove";
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
    public void visit(Script script) {
        if (script.getEvent() instanceof KeyPressed) {
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();
            if (listOfStmt.size() == 1) {
                Stmt stmt = listOfStmt.get(0);
                if (stmt instanceof MoveSteps || stmt instanceof ChangeXBy || stmt instanceof ChangeYBy) {
                    found = true;
                    count++;
                }
            }
        }

        if (!script.getChildren().isEmpty()) {
            for (ASTNode child : script.getChildren()) {
                child.accept(this);
            }
        }
    }
}
