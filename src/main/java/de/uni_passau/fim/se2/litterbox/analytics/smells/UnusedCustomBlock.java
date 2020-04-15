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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Checks if there are unused custom blocks in the project.
 */
public class UnusedCustomBlock implements IssueFinder, ScratchVisitor {

    public static final String NAME = "unused_custom_block";
    public static final String SHORT_NAME = "unusedCustBl";
    private static final String NOTE1 = "There are no uncalled custom blocks in your project.";
    private static final String NOTE2 = "Some of the custom blocks are never used.";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private List<String> proceduresDef;
    private List<String> calledProcedures;
    private Map<LocalIdentifier, ProcedureInfo> procMap;
    private Program program;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        count = 0;
        actorNames = new LinkedList<>();
        this.program = program;
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
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
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
