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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AmbiguousParameterName implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no ambiguous parameter names in your project.";
    private static final String NOTE2 = "Some of the procedures contain ambiguous parameter names.";
    public static final String NAME = "ambiguous_parameter_name";
    public static final String SHORT_NAME = "ambParamName";
    private boolean inStmtList = false;
    private boolean found = false;
    private boolean used = false;
    private int count = 0;
    private LinkedList<String> paraNames = new LinkedList<>();
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    private Map<Identifier, ProcedureInfo> procMap;
    private Program program;


    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
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
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
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
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        inStmtList = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        if (node.getStmtList().getStmts().getListOfStmt().size() > 0) {
            checkArguments(procMap.get(node.getIdent()).getArguments());
        }

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }

        if (used) {
            count++;
        }

        used = false;
        found = false;
        paraNames.clear();
    }

    @Override
    public void visit(StrId node) {
        if (inStmtList && found && paraNames.contains(node.getName())) {
            used = true;
        }

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
