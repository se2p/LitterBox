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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;


/**
 * The parameter names in custom blocks do not have to be unique.
 * Therefore, when two parameters have the same name, no matter the type or which one is used inside the custom
 * block, it will always be evaluated as the last input to the block.
 */
public class AmbiguousParameterName implements IssueFinder, ScratchVisitor {
    public static final String NAME = "ambiguous_parameter_name";
    public static final String SHORT_NAME = "ambParamName";
    public static final String HINT_TEXT = "ambiguous parameter name";
    private Set<Issue> issues = new LinkedHashSet<>();
    private int count = 0;
    private ActorDefinition currentActor;

    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        count = 0;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    private void checkArguments(ArgumentInfo[] arguments, ProcedureDefinition node) {
        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = 0; j < arguments.length; j++) {
                if (i != j && current.getName().equals(arguments[j].getName())) {
                    // TODO: Does this add redundant comments?
                    issues.add(new Issue(this, currentActor, node,
                            HINT_TEXT, SHORT_NAME + count, node.getMetadata().getDefinition()));
                    count++;
                }
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {

        if (!node.getStmtList().getStmts().isEmpty()) {
            checkArguments(procMap.get(node.getIdent()).getArguments(), node);
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
