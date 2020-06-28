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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * The parameter names in custom blocks do not have to be unique.
 * Therefore, when two parameters have the same name, no matter the type or which one is used inside the custom
 * block, it will always be evaluated as the last input to the block. For the strict version the parameters have to
 * be used to be counted.
 */
public class AmbiguousParameterNameStrict implements IssueFinder, ScratchVisitor {
    public static final String NAME = "ambiguous_parameter_name_strict";
    public static final String SHORT_NAME = "ambParamNameStrct";
    public static final String HINT_TEXT = "ambiguous parameter name strict";
    private boolean inStmtList = false;
    private boolean found = false;
    private boolean used = false;
    private int count = 0;
    private Set<Issue> issues = new LinkedHashSet<>();
    private LinkedList<String> paraNames = new LinkedList<>();
    private ActorDefinition currentActor;

    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        found = false;
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

        if (found) {
            found = false;
        }
    }

    private void checkArguments(ArgumentInfo[] arguments) {
        paraNames = new LinkedList<>();

        for (int i = 0; i < arguments.length; i++) {
            ArgumentInfo current = arguments[i];
            for (int j = 0; j < arguments.length; j++) {
                if (i != j && current.getName().equals(arguments[j].getName())) {
                    if (!paraNames.contains(current.getName())) {
                        paraNames.add(current.getName());
                    }
                    found = true;
                }
            }
        }
    }

    @Override
    public void visit(StmtList node) {
        inStmtList = true;
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        inStmtList = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        if (node.getStmtList().getStmts().size() > 0) {
            checkArguments(procMap.get(node.getIdent()).getArguments());
        }

        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }

        if (used) {
            issues.add(new Issue(this, currentActor, node));
            count++;
            addBlockComment((NonDataBlockMetadata) node.getMetadata().getDefinition(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }

        // TODO: This handling with used/found seems really error prone
        used = false;
        found = false;
        paraNames.clear();
    }

    @Override
    public void visit(StrId node) {
        if (inStmtList && found && paraNames.contains(node.getName())) {
            used = true;
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
