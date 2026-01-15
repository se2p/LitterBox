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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.AmbiguousCustomBlockSignature;
import de.uni_passau.fim.se2.litterbox.ast.builder.ProcedureBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injector for the {@link AmbiguousCustomBlockSignature} bug.
 *
 * <p>
 * This injector finds a {@link ProcedureDefinition} and duplicates it with the same name/signature
 * but a different (destructive) body. This creates two procedures with the same signature,
 * which the detector flags as ambiguous.
 *
 * <p>
 * The duplicate procedure contains a single {@code StopAll} statement, making it a realistic
 * but destructive variant.
 */
public class AmbiguousCustomBlockSignatureInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new AmbiguousCustomBlockSignature();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (!actor.getProcedureDefinitionList().getList().isEmpty()) {
            Set<String> calledProcedures = getCalledProcedureNames(actor);

            ProcedureDefinition originalTargetProc = findTargetProcedure(
                    actor.getProcedureDefinitionList().getList(), calledProcedures);
            String scriptId = "";
            if (originalTargetProc != null) {
                String id = AstNodeUtil.getBlockId(originalTargetProc);
                if (id != null) {
                    scriptId = id;
                }
            }

            modifyClonedNode(actor, clonedActor -> {
                Program clonedProgram = findProgramRoot(clonedActor);
                ProcedureDefinitionList clonedProcList = clonedActor.getProcedureDefinitionList();
                List<ProcedureDefinition> clonedProcedures = clonedProcList.getList();

                if (!clonedProcedures.isEmpty()) {
                    ProcedureDefinition targetProc = findTargetProcedure(clonedProcedures, calledProcedures);

                    if (targetProc != null) {
                        String proccode = getProcedureName(clonedProgram, clonedActor, targetProc);
                        ProcedureDefinition duplicate = createDestructiveDuplicate(targetProc);
                        clonedProcedures.addFirst(duplicate);
                        duplicate.setParentNode(clonedProcList);

                        // Register the duplicate with the same proccode to ensure collisions are detectable
                        registerProcedure(clonedProgram, clonedActor, duplicate, proccode);
                    }
                }
            }, scriptId);
        }

        super.visit(actor);
    }

    private Set<String> getCalledProcedureNames(ActorDefinition actor) {
        Set<String> calledProcedureNames = new HashSet<>();
        actor.accept(new ScratchVisitor() {
            @Override
            public void visit(CallStmt callStmt) {
                calledProcedureNames.add(callStmt.getIdent().getName());
            }
        });
        return calledProcedureNames;
    }

    private ProcedureDefinition findTargetProcedure(
            List<ProcedureDefinition> procedures, Set<String> calledProcedures) {
        // 1. Find the first procedure that is called AND has statements
        for (ProcedureDefinition proc : procedures) {
            if (calledProcedures.contains(proc.getIdent().getName())
                    && proc.getStmtList().hasStatements()) {
                return proc;
            }
        }

        // 2. Fallback: If no such procedure found, try any called procedure
        for (ProcedureDefinition proc : procedures) {
            if (calledProcedures.contains(proc.getIdent().getName())) {
                return proc;
            }
        }

        // 3. Fallback: Any procedure with statements
        for (ProcedureDefinition proc : procedures) {
            if (proc.getStmtList().hasStatements()) {
                return proc;
            }
        }

        // 4. Final Fallback: Just the first one
        return procedures.isEmpty() ? null : procedures.getFirst();
    }

    private ProcedureDefinition createDestructiveDuplicate(ProcedureDefinition original) {
        StopAll stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
        return new ProcedureBuilder()
                .from(original)
                .withFreshMetadata()
                .withIdent(new StrId(CloneVisitor.generateUID()))
                .withBody(stopAll)
                .build();
    }
}
