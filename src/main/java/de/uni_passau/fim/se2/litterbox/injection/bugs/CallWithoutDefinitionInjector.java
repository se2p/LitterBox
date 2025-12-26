/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.util.TypoGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Injector for the {@link CallWithoutDefinition} bug.
 * <p>
 * This injector finds {@link CallStmt} nodes and introduces a typo into the procedure name,
 * resulting in a call to a procedure that does not exist. The typo is generated using
 * {@link TypoGenerator} and verified to be unique (not matching any existing procedure name).
 */
public class CallWithoutDefinitionInjector extends AbstractBugInjector {

    private final Set<String> existingProcedures = new HashSet<>();
    private Program program;

    @Override
    public IssueFinder getIssueFinder() {
        return new CallWithoutDefinition();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        this.program = program;

        // Pre-populate existingProcedures with proccodes BEFORE the main traversal
        existingProcedures.clear();
        program.accept(new ProcedureCollector());

        return super.inject(program);
    }

    /**
     * Visitor that collects all procedure proccodes in the program.
     * Uses the procedure mapping to get the human-readable proccode.
     */
    private class ProcedureCollector implements ScratchVisitor {
        @Override
        public void visit(ProcedureDefinition node) {
            String proccode = getProccode(node);
            existingProcedures.add(proccode);
            visitChildren(node);
        }

        private String getProccode(ProcedureDefinition proc) {
            // For programs parsed from test strings, the ident name IS the proccode
            // For programs loaded from JSON, we need to look up in the mapping
            if (program.getProcedureMapping() == null) {
                return proc.getIdent().getName();
            }

            // Try to find the proccode via the procedure mapping
            var actorOpt = AstNodeUtil.findActor(proc);
            if (actorOpt.isPresent()) {
                ActorDefinition actor = actorOpt.get();
                Map<LocalIdentifier, ProcedureInfo> actorProcs =
                        program.getProcedureMapping().getProceduresForActor(actor.getIdent().getName());
                if (actorProcs != null) {
                    ProcedureInfo info = actorProcs.get(proc.getIdent());
                    if (info != null) {
                        return info.getName();
                    }
                }
            }

            // Fallback to ident name (test programs, or edge cases)
            return proc.getIdent().getName();
        }
    }

    @Override
    public void visit(CallStmt node) {
        String callName = node.getIdent().getName();

        // Skip if this call is already orphaned (calling an undefined procedure)
        // Only inject on calls that have a matching definition
        if (!existingProcedures.contains(callName)) {
            super.visit(node);
            return;
        }

        mutateClone(node, clonedCall -> {
            String uniqueTypo = generateUniqueTypo(clonedCall.getIdent().getName());
            CallStmt newCall = new CallStmt(
                    new StrId(uniqueTypo),
                    clonedCall.getExpressions(),
                    clonedCall.getMetadata()
            );
            replaceInParent(clonedCall, newCall);
        });
        super.visit(node);
    }

    private String generateUniqueTypo(String originalName) {
        String typo = TypoGenerator.generateTypo(originalName);
        while (existingProcedures.contains(typo)) {
            typo = TypoGenerator.generateTypo(typo);
        }
        return typo;
    }
}


