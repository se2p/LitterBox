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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if there are unused custom blocks in the project.
 */
public class UnusedCustomBlock extends AbstractIssueFinder {

    public static final String NAME = "unused_custom_block";
    private List<ProcedureDefinition> proceduresDef;
    private List<String> calledProcedures;

    private void checkCalls() {
        for (ProcedureDefinition procedureDef : proceduresDef) {
            ProcedureInfo info = procMap.get(procedureDef.getIdent());
            if (!calledProcedures.contains(info.getName())) {
                addIssue(procedureDef, ((ProcedureMetadata) procedureDef.getMetadata()).getDefinition());
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
        proceduresDef.add(node);
        super.visit(node);
    }

    @Override
    public void visit(CallStmt node) {
        calledProcedures.add(node.getIdent().getName());
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
