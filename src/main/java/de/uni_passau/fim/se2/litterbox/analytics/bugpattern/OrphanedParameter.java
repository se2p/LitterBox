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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * When custom blocks are created the user can define parameters, which can then be used in the body of the custom
 * block.
 * However, the block definition can be altered, including removal of parameters even if they are in use.
 * Any instances of deleted parameters are retained, and then evaluated with the standard value for the type of
 * parameter, since they are never initialised.
 */
public class OrphanedParameter implements IssueFinder, ScratchVisitor {
    public static final String NAME = "orphaned_parameter";
    public static final String SHORT_NAME = "orphParam";
    public static final String HINT_TEXT = "orphaned parameter";
    private ActorDefinition currentActor;
    private List<ParameterDefinition> currentParameterDefinitions;
    private boolean insideProcedure;
    private Set<Issue> issues = new LinkedHashSet<>();

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
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (insideProcedure) {
            checkParameterNames(node.getName().getName(), node);
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    private void checkParameterNames(String name, Parameter node) {
        boolean validParametername = false;
        for (int i = 0; i < currentParameterDefinitions.size() && !validParametername; i++) {
            if (name.equals(currentParameterDefinitions.get(i).getIdent().getName())) {
                validParametername = true;
            }
        }
        if (!validParametername) {
            issues.add(new Issue(this, currentActor, node,
                    HINT_TEXT, node.getMetadata()));
        }
    }
}
