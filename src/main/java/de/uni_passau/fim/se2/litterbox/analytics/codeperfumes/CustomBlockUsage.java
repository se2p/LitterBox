/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Blocks must be defined and then can be called and used by a script.
 */
public class CustomBlockUsage extends AbstractIssueFinder {

    public static final String NAME = "custom_block_usage";
    private List<String> proceduresDef;
    private List<CallStmt> calledProcedures;

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (proceduresDef.contains(calledProcedure.getIdent().getName())
                    || program.getProcedureMapping().checkIfMalformed(
                    currentActor.getIdent().getName() + calledProcedure.getIdent().getName())) {

                addIssue(calledProcedure, calledProcedure.getMetadata(), IssueSeverity.LOW);
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
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
