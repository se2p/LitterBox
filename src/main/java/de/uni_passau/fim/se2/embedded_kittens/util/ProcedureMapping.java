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
package de.uni_passau.fim.se2.embedded_kittens.util;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public final class ProcedureMapping {

    private final ProcedureDefinitionNameMapping procedureMapping;
    private final Map<LocalIdentifier, ProcedureDefinition> procedures;

    public ProcedureMapping(final Program program) {
        this.procedureMapping = program.getProcedureMapping();

        final ProcedureDefinitionVisitor pdv = new ProcedureDefinitionVisitor();
        program.accept(pdv);
        this.procedures = pdv.procedures;
    }

    /**
     * Finds the procedure definition that is called by this custom block.
     *
     * @param callStmt Some custom block call statement.
     * @return The corresponding procedure definition.
     */
    public Optional<ProcedureDefinition> findCalledProcedure(final CallStmt callStmt) {
        String procedureName = callStmt.getIdent().getName();
        String sprite = AstNodeUtil.findActor(callStmt).orElseThrow().getIdent().getName();

        return procedureMapping.getProceduresForName(sprite, procedureName)
            .stream()
            .filter(procedure -> hasMatchingParameterCount(callStmt, procedure.getRight()))
            .map(org.apache.commons.lang3.tuple.Pair::getKey)
            .map(procedures::get)
            .findFirst();
    }

    public String getName(final ProcedureDefinition procedureDefinition) {
        return procedureMapping.getProcedureInfo(procedureDefinition).getName();
    }

    private static boolean hasMatchingParameterCount(final CallStmt callStmt, final ProcedureInfo procedure) {
        int passedArgumentCount = callStmt.getExpressions().getExpressions().size();
        int acceptingArgumentCount = procedure.getArguments().length;
        return passedArgumentCount == acceptingArgumentCount;
    }

    private static class ProcedureDefinitionVisitor implements ScratchVisitor {

        final Map<LocalIdentifier, ProcedureDefinition> procedures = new IdentityHashMap<>();

        @Override
        public void visit(ProcedureDefinitionList node) {
            for (ProcedureDefinition procedureDefinition : node.getList()) {
                procedures.put(procedureDefinition.getIdent(), procedureDefinition);
            }
        }
    }
}
