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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OrphanedParameter;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injector for the {@link OrphanedParameter} bug.
 * <p>
 * This injector finds procedure definitions with parameters that are used in the body,
 * and removes one such used parameter from the signature, creating an orphaned parameter reference.
 */
public class OrphanedParameterInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new OrphanedParameter();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        if (node.getParameterDefinitionList() == null
                || node.getParameterDefinitionList().getParameterDefinitions().isEmpty()) {
            super.visit(node);
            return;
        }

        // Find parameters that are actually used in the procedure body
        Set<String> usedParameterNames = findUsedParameters(node);

        // Find the first parameter definition that is used in the body
        List<ParameterDefinition> params = node.getParameterDefinitionList().getParameterDefinitions();
        ParameterDefinition usedParam = null;
        int usedParamIndex = -1;

        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i).getIdent().getName();
            if (usedParameterNames.contains(paramName)) {
                usedParam = params.get(i);
                usedParamIndex = i;
                break;
            }
        }

        // Only inject if we found a used parameter to remove
        if (usedParam != null) {
            final int indexToRemove = usedParamIndex;
            mutateClone(node, clonedNode -> {
                if (indexToRemove < clonedNode.getParameterDefinitionList().getParameterDefinitions().size()) {
                    clonedNode.getParameterDefinitionList().getParameterDefinitions().remove(indexToRemove);
                }
            });
        }

        super.visit(node);
    }

    /**
     * Finds the names of all parameters that are used in the procedure body.
     */
    private Set<String> findUsedParameters(ProcedureDefinition node) {
        UsedParameterFinder finder = new UsedParameterFinder();
        node.getStmtList().accept(finder);
        return finder.getUsedParameterNames();
    }

    /**
     * Visitor that collects names of Parameter references in the procedure body.
     */
    private static class UsedParameterFinder implements ScratchVisitor {
        private final Set<String> usedParameterNames = new HashSet<>();

        @Override
        public void visit(Parameter node) {
            usedParameterNames.add(node.getName().getName());
        }

        public Set<String> getUsedParameterNames() {
            return usedParameterNames;
        }
    }
}
