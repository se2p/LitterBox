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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * When a custom block is called without being defined nothing happens. This can occur in two different situations:
 * 1) When the definition of a custom block is deleted in the editor, the call block remains in the code column and
 * can still be used. 2) A script using a call to a custom block can be dragged and copied to another sprite,
 * probably no custom block with the same signature as the call exists here and thus the call has no definition.
 */
public class CallWithoutDefinition implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no calls without definitions in your project.";
    private static final String NOTE2 = "Some of the sprites contain calls without definitions.";
    public static final String NAME = "call_without_definition";
    public static final String SHORT_NAME = "cllWithoutDef";
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
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        calledProcedures = new ArrayList<>();
        proceduresDef = new ArrayList<>();
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
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
        for (String calledProcedure : calledProcedures) {
            if (!proceduresDef.contains(calledProcedure)) {
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
