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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * When a custom block is called without being defined nothing happens. This can occur in two different situations:
 * 1) When the definition of a custom block is deleted in the editor, the call block remains in the code column and
 * can still be used. 2) A script using a call to a custom block can be dragged and copied to another sprite,
 * probably no custom block with the same signature as the call exists here and thus the call has no definition.
 */
public class CallWithoutDefinition extends AbstractIssueFinder {
    public static final String NAME = "call_without_definition";
    private List<String> proceduresDef;
    private List<CallStmt> calledProcedures;

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (!proceduresDef.contains(calledProcedure.getIdent().getName())
                    && !program.getProcedureMapping().checkIfMalformated(
                    currentActor.getIdent().getName() + calledProcedure.getIdent().getName())) {

                addIssue(calledProcedure, calledProcedure.getMetadata());
            }
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        calledProcedures = new ArrayList<>();
        proceduresDef = new ArrayList<>();
        super.visit(actor);
        checkCalls();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        proceduresDef.add(procMap.get(node.getIdent()).getName());
        super.visit(node);
    }

    @Override
    public void visit(CallStmt node) {
        calledProcedures.add(node);
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
