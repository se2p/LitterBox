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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Names for custom blocks are non-unique. Two custom blocks with the same name can only be distinguished if they have a
 * different number or order of parameters.
 * When two blocks have the same name and parameter order, no matter which call block is used, the program will
 * always call the matching custom block which was defined earlier.
 */
public class AmbiguousCustomBlockSignature extends AbstractIssueFinder {
    public static final String NAME = "ambiguous_custom_block_signature";

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
        LocalIdentifier ident = node.getIdent();
        List<ProcedureInfo> procedureInfos = new ArrayList<>(procMap.values());
        ProcedureInfo current = procMap.get(ident);
        for (ProcedureInfo procedureInfo : procedureInfos) {
            if (procedureInfo != current && current.getName().equals(procedureInfo.getName())
                    && current.getActorName().equals(procedureInfo.getActorName())) {
                addIssue(node, ((ProcedureMetadata) node.getMetadata()).getDefinition());
            }
        }
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
