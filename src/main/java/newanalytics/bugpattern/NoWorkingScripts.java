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
import newanalytics.smells.DeadCode;
import newanalytics.smells.EmptyScript;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.event.Event;
import scratch.ast.model.event.Never;
import scratch.ast.model.procedure.Parameter;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class NoWorkingScripts implements IssueFinder, ScratchVisitor {
    public static final String NAME = "Simultaneous_empty_sprite_and_dead_code";
    public static final String SHORT_NAME = "simemptscrptdcode";
    private static final String NOTE1 = "There are no sprites with empty scripts and simultaneously dead code in your project.";
    private static final String NOTE2 = "Some of the sprites contain empty scripts and simultaneously dead code.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private boolean stillFullfilled = true;

    public NoWorkingScripts() {
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
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
    public void visit(Event node) {
        if (stillFullfilled) {
            if (node instanceof Never) {
              //  if((Never) node)
            } else {

            }
        }
    }
}
