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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;

import java.util.HashSet;
import java.util.Set;

public class DuplicateSprite extends AbstractIssueFinder {

    private static final String NAME = "duplicate_sprite";

    @Override
    public void visit(Program node) {
        Set<ActorDefinition> checked = new HashSet<>();
        ActorDefinitionList actors = node.getActorDefinitionList();
        for (ActorDefinition actor : actors.getDefinitions()) {
            for (ActorDefinition other : actors.getDefinitions()) {
                if (actor == other || checked.contains(other)) {
                    continue;
                }

                if (areActorsIdentical(actor, other)) {
                    currentActor = actor;
                    procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
                    addIssueWithLooseComment();
                }
            }
            checked.add(actor);
        }
    }

    private boolean areActorsIdentical(ActorDefinition actor, ActorDefinition other) {
        boolean equalScripts = actor.getScripts().equals(other.getScripts());
        boolean equalProcedures = compareProcedureDefinitions(actor.getProcedureDefinitionList(), other.getProcedureDefinitionList());
        boolean equalDeclarations = actor.getDecls().equals(other.getDecls());
        boolean equalActorType = actor.getActorType().equals(other.getActorType());

        return equalActorType && equalDeclarations && equalProcedures && equalScripts;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private boolean compareProcedureDefinitions(ProcedureDefinitionList procList1, ProcedureDefinitionList procList2) {
        if (procList1.getList().size() != procList2.getList().size()) {
            return false;
        }

        // For each procedure in list1 there has to be one with the same parameters
        // and the same code in the other
        for (ProcedureDefinition procDef1 : procList1.getList()) {
            boolean hasMatching = false;

            for (ProcedureDefinition procDef2 : procList2.getList()) {
                if (!procDef1.getParameterDefinitionList().equals(procDef2.getParameterDefinitionList())) {
                    continue;
                }
                if (!procDef1.getStmtList().equals(procDef2.getStmtList())) {
                    continue;
                }
                hasMatching = true;
                break;
            }
            if (!hasMatching) {
                return false;
            }
        }

        return true;
    }

}
