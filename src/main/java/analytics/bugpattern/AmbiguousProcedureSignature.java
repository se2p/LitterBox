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

import analytics.IssueFinder;
import analytics.IssueReport;
import ast.model.ASTNode;
import ast.model.ActorDefinition;
import ast.model.Program;
import ast.model.procedure.ProcedureDefinition;
import ast.model.variable.Identifier;
import ast.parser.symboltable.ProcedureInfo;
import ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AmbiguousProcedureSignature implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no ambiguous procedure signatures in your project.";
    private static final String NOTE2 = "Some of the procedures signatures are ambiguous.";
    public static final String NAME = "ambiguous_procedure_signature";
    public static final String SHORT_NAME = "ambProcSign";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private List<String> procNames;
    private Map<Identifier, ProcedureInfo> procMap;
    private Program program;


    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        procNames = new LinkedList<>();
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
        procNames = new LinkedList<>();
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

    @Override
    public void visit(ProcedureDefinition node) {
        if (node.getStmtList().getStmts().getListOfStmt().size() > 0) {
            checkProc(node.getIdent());
        }

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    private void checkProc(Identifier ident) {
        List<ProcedureInfo> procedureInfos = new ArrayList<>(procMap.values());
        ProcedureInfo current = procMap.get(ident);
        for (ProcedureInfo procedureInfo : procedureInfos) {
            if (procedureInfo != current && current.getName().equals(procedureInfo.getName())
                    && current.getActorName().equals(procedureInfo.getActorName())) {
                found = true;
                count++;
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
