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

import java.util.LinkedList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.event.StartedAsClone;
import scratch.ast.model.statement.common.CreateCloneOf;
import scratch.ast.model.variable.StrId;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class RecursiveCloning implements ScratchVisitor, IssueFinder {
    private static final String NOTE1 = "There are no recursive cloning calls in your project.";
    private static final String NOTE2 = "Some of the sprites contain recursive cloning calls.";
    public static final String NAME = "recursive_cloning";
    public static final String SHORT_NAME = "recClone";
    private boolean found = false;
    private boolean startAsClone = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        startAsClone = false;
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
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            startAsClone = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        startAsClone = false;
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (startAsClone) {
            if (node.getStringExpr() instanceof StrId) {

                if (((StrId) node.getStringExpr()).getName().equals("_myself_")) {
                    count++;
                    found = true;
                }
            }

        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}