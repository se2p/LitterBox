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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class NoWorkingScripts implements IssueFinder, ScratchVisitor {
    public static final String NAME = "no_working_scripts";
    public static final String SHORT_NAME = "noWorkScript";
    private static final String NOTE1 = "There are no sprites with only empty scripts and simultaneously dead code in your project.";
    private static final String NOTE2 = "Some of the sprites contain only empty scripts and simultaneously dead code.";
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private boolean stillFullfilledEmptyScript = false;
    private boolean deadCodeFound = false;
    private boolean foundEvent = false;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
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
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        foundEvent = false;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (deadCodeFound && stillFullfilledEmptyScript && foundEvent) {
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
                foundEvent = true;
                if (node.getStmtList().getStmts().getListOfStmt().size() > 0) {
                    stillFullfilledEmptyScript = false;
                }
            }
        }
    }
}
