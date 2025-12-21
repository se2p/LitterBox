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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.util.TypoGenerator;

import java.util.HashSet;
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

    @Override
    public IssueFinder getIssueFinder() {
        return new CallWithoutDefinition();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        existingProcedures.add(node.getIdent().getName());
        super.visit(node);
    }

    @Override
    public void visit(CallStmt node) {
        addMutation(node, clonedCall -> {
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
