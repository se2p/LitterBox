/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;

import java.util.List;

/**
 * Names for custom blocks are non-unique. Two custom blocks with the same name can only be distinguished if they have a
 * different number or order of parameters.
 * When two blocks have the same name and parameter order, no matter which call block is used, the program will
 * always call the matching custom block which was defined earlier.
 */
public class AmbiguousCustomBlockSignature extends AbstractIssueFinder {
    public static final String NAME = "ambiguous_custom_block_signature";

    private List<ProcedureDefinition> currentActorProcedures;

    @Override
    public void visit(ActorDefinition actor) {
        currentActorProcedures = actor.getProcedureDefinitionList().getList();
        super.visit(actor);
        currentActorProcedures = null;
    }

    @Override
    public void visit(Script node) {
        //NOP should not be detected in Scripts
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        if (node.getStmtList().hasStatements()) {
            checkProc(node);
        }

        visitChildren(node);
        currentProcedure = null;
    }

    private void checkProc(ProcedureDefinition node) {
        if (currentActorProcedures == null) {
            return;
        }

        String currentProcName = getProcName(node);
        if (currentProcName == null) {
            return;
        }

        for (ProcedureDefinition other : currentActorProcedures) {
            if (other != node) {
                String otherProcName = getProcName(other);
                if (currentProcName.equals(otherProcName)) {
                    addIssue(node, node.getMetadata().getDefinition(), IssueSeverity.MEDIUM);
                    return; // Only report once per procedure
                }
            }
        }
    }

    /**
     * Gets the procedure name (proccode) for comparison.
     * Uses procMap lookup from the symbol table.
     */
    private String getProcName(ProcedureDefinition proc) {
        ProcedureInfo info = procMap.get(proc.getIdent());
        return info != null ? info.getName() : null;
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

