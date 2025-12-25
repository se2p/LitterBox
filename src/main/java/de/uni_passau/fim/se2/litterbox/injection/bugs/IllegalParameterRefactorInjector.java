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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.IllegalParameterRefactor;
import de.uni_passau.fim.se2.litterbox.ast.builder.ProcedureBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injects IllegalParameterRefactor bug by changing a Boolean parameter type to String.
 * <p>
 * Note: Uses a custom CloneVisitor because ProcedureDefinition has immutable fields
 * and requires rebuilding with modified parameters.
 */
public class IllegalParameterRefactorInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new IllegalParameterRefactor();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        CustomMappingCloneVisitor visitor = new CustomMappingCloneVisitor();
        Program cloned = (Program) program.accept(visitor);
        if (visitor.isModified()) {
            cloned.accept(new ParentVisitor());
            return singleResult(cloned);
        }
        return Collections.emptyList();
    }

    private static class CustomMappingCloneVisitor extends CloneVisitor {
        private boolean modified = false;

        public boolean isModified() {
            return modified;
        }

        @Override
        public ASTNode visit(ProcedureDefinition node) {
            List<ParameterDefinition> clonedParams = applyList(node.getParameterDefinitionList().getParameterDefinitions());

            // Find parameters used in boolean contexts
            Set<String> usedAsBool = findParametersUsedAsBool(node);

            // Find a boolean parameter that is used as a boolean to corrupt
            for (int i = 0; i < clonedParams.size(); i++) {
                ParameterDefinition param = clonedParams.get(i);
                if (param.getType() instanceof BooleanType && usedAsBool.contains(param.getIdent().getName())) {
                    // Change type to String to cause illegal usage if used as boolean
                    ParameterDefinition mutated = new ParameterDefinition(
                            apply(param.getIdent()),
                            new StringType(),
                            apply(param.getMetadata())
                    );
                    clonedParams.set(i, mutated);
                    modified = true;
                    break; // Only mutate one
                }
            }

            return new ProcedureBuilder()
                    .withIdent(apply(node.getIdent()))
                    .withParameters(clonedParams)
                    .withBody(apply(node.getStmtList()))
                    .withMetadata(apply(node.getMetadata()))
                    .build();
        }

        private Set<String> findParametersUsedAsBool(ProcedureDefinition node) {
            Set<String> usedAsBool = new HashSet<>();
            node.getStmtList().accept(new de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor() {
                @Override
                public void visit(de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool asBool) {
                    if (asBool.getOperand1() instanceof Parameter param) {
                        usedAsBool.add(param.getName().getName());
                    }
                    visitChildren(asBool);
                }
            });
            return usedAsBool;
        }
    }
}
