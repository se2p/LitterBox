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
import scratch.ast.model.event.Never;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class NoWorkingScripts implements IssueFinder, ScratchVisitor {
    public static final String NAME = "No_Working_Scripts";
    public static final String SHORT_NAME = "noWorkScript";
    private static final String NOTE1 = "There are no sprites with only empty scripts and simultaneously dead code in your project.";
    private static final String NOTE2 = "Some of the sprites contain only empty scripts and simultaneously dead code.";
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private boolean stillFullfilledEmptyScript = true;
    private boolean deadCodeFound = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        actorNames = new LinkedList<>();

        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
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
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (deadCodeFound && stillFullfilledEmptyScript) {
            actorNames.add(currentActor.getIdent().getName());
            count++;
        }
    }

    @Override
    public void visit(Script node) {
        if (stillFullfilledEmptyScript) {
            if (node.getEvent() instanceof Never) {
                if (node.getStmtList().getStmts().getListOfStmt().size() > 0) {
                    deadCodeFound = true;
                }
            } else {
                if (node.getStmtList().getStmts().getListOfStmt().size() > 0) {
                    stillFullfilledEmptyScript = false;
                }
            }
        }
    }
}
