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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.IllegalParameterRefactor;
import de.uni_passau.fim.se2.litterbox.ast.builder.ProcedureBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injects IllegalParameterRefactor bug by changing a Boolean parameter type to String.
 *
 * <p>
 * Note: Uses a custom CloneVisitor because ProcedureDefinition has immutable fields
 * and requires rebuilding with modified parameters.
 */
public class IllegalParameterRefactorInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new IllegalParameterRefactor();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        Set<String> usedAsBool = findParametersUsedAsBool(node);

        // Check if we have any valid candidates before attempting mutation
        boolean hasCandidate = node.getParameterDefinitionList().getParameterDefinitions().stream()
                .anyMatch(p -> p.getType() instanceof BooleanType && usedAsBool.contains(p.getIdent().getName()));

        if (hasCandidate) {
            replaceNodeImmutably(node, original -> {
                List<ParameterDefinition> clonedParams = cloner.applyList(
                        original.getParameterDefinitionList().getParameterDefinitions());
                boolean mutated = false;

                for (int i = 0; i < clonedParams.size(); i++) {
                    ParameterDefinition param = clonedParams.get(i);
                    // Match by name with the usedAsBool set which comes from original node analysis
                    if (param.getType() instanceof BooleanType && usedAsBool.contains(param.getIdent().getName())) {
                        // Change type to String
                        ParameterDefinition newParam = new ParameterDefinition(
                                cloner.apply(param.getIdent()),
                                new StringType(),
                                cloner.apply(param.getMetadata())
                        );
                        clonedParams.set(i, newParam);
                        mutated = true;
                        break; // Only mutate one
                    }
                }

                if (!mutated) {
                    // Should not happen if preconditions checked, but safe fallback
                    return cloner.apply(original);
                }

                return new ProcedureBuilder()
                        .withIdent(cloner.apply(original.getIdent()))
                        .withParameters(clonedParams)
                        .withBody(cloner.apply(original.getStmtList()))
                        .withMetadata(cloner.apply(original.getMetadata()))
                        .build();
            });
        }
        super.visit(node);
    }

    private Set<String> findParametersUsedAsBool(ProcedureDefinition node) {
        BoolParameterCollector collector = new BoolParameterCollector();
        node.getStmtList().accept(collector);
        return collector.getParametersUsedAsBool();
    }

    /**
     * Visitor that collects parameter names used within AsBool expressions.
     * A parameter wrapped in AsBool indicates boolean usage, which becomes
     * problematic if the parameter type is changed to String.
     */
    private static class BoolParameterCollector implements ScratchVisitor {
        private final Set<String> usedAsBool = new HashSet<>();

        @Override
        public void visit(AsBool asBool) {
            if (asBool.getOperand1() instanceof Parameter param) {
                usedAsBool.add(param.getName().getName());
            }
            visitChildren(asBool);
        }

        public Set<String> getParametersUsedAsBool() {
            return usedAsBool;
        }
    }
}
