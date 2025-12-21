/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.Collections;
import java.util.List;

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
    public List<Program> inject(Program program) {
        CustomCloneVisitor visitor = new CustomCloneVisitor();
        Program cloned = (Program) program.accept(visitor);
        return Collections.singletonList(cloned);
    }

    private static class CustomCloneVisitor extends CloneVisitor {
        @Override
        public ASTNode visit(ProcedureDefinition node) {
            List<ParameterDefinition> clonedParams = applyList(node.getParameterDefinitionList().getParameterDefinitions());

            // Find a boolean parameter to corrupt
            for (int i = 0; i < clonedParams.size(); i++) {
                ParameterDefinition param = clonedParams.get(i);
                if (param.getType() instanceof BooleanType) {
                    // Change type to String to cause illegal usage if used as boolean
                    ParameterDefinition mutated = new ParameterDefinition(
                            apply(param.getIdent()),
                            new StringType(),
                            apply(param.getMetadata())
                    );
                    clonedParams.set(i, mutated);
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
    }
}
