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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The parameters of a custom block can be used anywhere inside the sprite that defines the custom block.
 * However, they will never be initialised outside the custom block, and will always have the default value.
 */
public class ParameterOutOfScope implements IssueFinder, ScratchVisitor {
    public static final String NAME = "parameter_out_of_scope";
    public static final String SHORT_NAME = "paramOutScope";
    public static final String HINT_TEXT = "parameter out of scope";
    private ActorDefinition currentActor;
    private Set<Issue> issues = new LinkedHashSet<>();
    private boolean insideProcedure;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        program.accept(this);
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (!insideProcedure) {
            issues.add(new Issue(this, currentActor, node,
                    HINT_TEXT, node.getMetadata()));
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}
