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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import analytics.IssueFinder;
import analytics.IssueReport;
import ast.model.ASTNode;
import ast.model.ActorDefinition;
import ast.model.Program;
import ast.model.event.Clicked;
import ast.model.event.StartedAsClone;
import ast.model.statement.common.CreateCloneOf;
import ast.model.variable.StrId;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class MissingCloneInitialization implements IssueFinder, ScratchVisitor {

    public static final String NAME = "missing_clone_initialization";
    public static final String SHORT_NAME = "mssCloneInit";

    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        program.accept(this);
        final List<String> uninitializingActors
                = clonedActors.stream().filter(s -> !whenStartsAsCloneActors.contains(s)).collect(Collectors.toList());

        return new IssueReport(NAME, uninitializingActors.size(), uninitializingActors, "");
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
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (node.getStringExpr() instanceof StrId) {
            final String spriteName = ((StrId) node.getStringExpr()).getName();
            if (spriteName.equals("_myself_")) {
                clonedActors.add(currentActor.getIdent().getName());
            } else {
                clonedActors.add(spriteName);
            }
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        whenStartsAsCloneActors.add(currentActor.getIdent().getName());
    }

    @Override
    public void visit(Clicked node) {
        whenStartsAsCloneActors.add(currentActor.getIdent().getName());
    }
}
