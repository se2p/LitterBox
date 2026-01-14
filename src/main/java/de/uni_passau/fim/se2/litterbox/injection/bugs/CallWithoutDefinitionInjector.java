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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.util.TypoGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Injector for the {@link CallWithoutDefinition} bug.
 *
 * <p>
 * This injector finds {@link CallStmt} nodes and introduces a typo into the procedure name,
 * resulting in a call to a procedure that does not exist. The typo is generated using
 * {@link TypoGenerator} and verified to be unique (not matching any existing procedure name).
 */
public class CallWithoutDefinitionInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new CallWithoutDefinition();
    private Program program;

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        this.program = program;
        return super.inject(program);
    }

    @Override
    public void visit(CallStmt node) {
        String callName = node.getIdent().getName();

        // Skip if this call is already orphaned (calling an undefined procedure in THIS actor)
        // Only inject on calls that have a matching definition
        if (!isDefinedInCurrentActor(node, callName)) {
            super.visit(node);
            return;
        }

        // Collect all existing procedure names for unique typo generation
        Set<String> existingProcedures = collectAllProcedureNames();

        replaceNodeImmutably(node, originalCall -> {
            String uniqueTypo = generateUniqueTypo(originalCall.getIdent().getName(), existingProcedures);
            return new CallStmt(
                    new StrId(uniqueTypo),
                    originalCall.getExpressions(),
                    originalCall.getMetadata()
            );
        });
        super.visit(node);
    }

    /**
     * Check if a procedure with the given name is defined in the actor containing the call.
     */
    private boolean isDefinedInCurrentActor(CallStmt node, String procName) {
        var actorOpt = AstNodeUtil.findActor(node);
        if (actorOpt.isEmpty() || program.getProcedureMapping() == null) {
            return false;
        }

        ActorDefinition actor = actorOpt.get();
        Map<LocalIdentifier, ProcedureInfo> actorProcs =
                program.getProcedureMapping().getProceduresForActor(actor.getIdent().getName());
        if (actorProcs == null) {
            return false;
        }

        // Check if any procedure in this actor has this name
        return actorProcs.values().stream()
                .anyMatch(info -> info.getName().equals(procName));
    }

    /**
     * Collect all procedure names from all actors for uniqueness checking.
     */
    private Set<String> collectAllProcedureNames() {
        Set<String> names = new HashSet<>();
        if (program.getProcedureMapping() != null) {
            for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
                Map<LocalIdentifier, ProcedureInfo> actorProcs =
                        program.getProcedureMapping().getProceduresForActor(actor.getIdent().getName());
                if (actorProcs != null) {
                    for (ProcedureInfo info : actorProcs.values()) {
                        names.add(info.getName());
                    }
                }
            }
        }
        return names;
    }

    private String generateUniqueTypo(String originalName, Set<String> existingProcedures) {
        // Find where parameter placeholders start
        int placeholderStart = originalName.indexOf('%');

        String prefix;
        String suffix;
        if (placeholderStart == -1) {
            // No placeholders - apply typo to entire name
            prefix = originalName;
            suffix = "";
        } else {
            // Split: apply typo only to prefix, preserve suffix with placeholders
            prefix = originalName.substring(0, placeholderStart);
            suffix = originalName.substring(placeholderStart);
        }

        String typoPrefix = TypoGenerator.generateTypo(prefix);
        String typo = typoPrefix + suffix;
        while (existingProcedures.contains(typo)) {
            typoPrefix = TypoGenerator.generateTypo(typoPrefix);
            typo = typoPrefix + suffix;
        }
        return typo;
    }
}
