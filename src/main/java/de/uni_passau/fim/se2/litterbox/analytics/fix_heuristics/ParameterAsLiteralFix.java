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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fix heuristic for the ParameterAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a parameter name.
 * The bug is fixed if the literal no longer matches any parameter name.
 */
public class ParameterAsLiteralFix extends AbstractFixHeuristic {
    public static final String NAME = "parameter_as_literal_fix";

    private Set<String> parametersInScope;

    public ParameterAsLiteralFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        parametersInScope = node.getParameterDefinitionList().getParameterDefinitions().stream()
                .map(p -> p.getIdent().getName())
                .collect(Collectors.toSet());

        super.visit(node);

        parametersInScope = null;
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            // Only report fix if inside a procedure AND text no longer matches parameter
            // If not in procedure scope, don't report (the code was moved/deleted, not fixed)
            if (parametersInScope != null && !parametersInScope.contains(node.getText())) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
