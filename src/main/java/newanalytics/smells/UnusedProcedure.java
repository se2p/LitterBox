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
package newanalytics.smells;

import java.util.*;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.statement.CallStmt;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.symboltable.ProcedureInfo;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

/**
 * Checks if there are unused custom blocks in the project.
 */
public class UnusedProcedure implements IssueFinder, ScratchVisitor {

    private static final String NOTE1 = "There are no uncalled procedures in your project.";
    private static final String NOTE2 = "Some of the procedures are never used.";
    public static final String NAME = "unused_procedure";
    public static final String SHORT_NAME = "unusedProc";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private List<String> proceduresDef;
    private List<String> calledProcedures;
    private Map<Identifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        this.program=program;
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
        procMap=program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        calledProcedures = new ArrayList<>();
        proceduresDef = new ArrayList<>();
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
        checkCalls();
        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    private void checkCalls() {
        for (String procedureDef : proceduresDef) {
            if (!calledProcedures.contains(procedureDef)) {
                found = true;
                count++;
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {

        proceduresDef.add(procMap.get(node.getIdent()).getName());

        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(CallStmt node) {
        calledProcedures.add(node.getIdent().getName());
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
