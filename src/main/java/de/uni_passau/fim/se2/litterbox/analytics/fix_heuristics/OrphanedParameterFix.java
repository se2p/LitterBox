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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OrphanedParameter;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.List;

/**
 * Fix heuristic for the OrphanedParameter bug.
 *
 * <p>The bug is reported when a parameter is used but not in procedure signature.
 * The bug is fixed if the parameter is now in the signature.
 */
public class OrphanedParameterFix extends AbstractFixHeuristic {

    private List<ParameterDefinition> currentParameterDefinitions;

    private final OrphanedParameter relatedBugFinder = new OrphanedParameter();

    public OrphanedParameterFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        currentParameterDefinitions = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        visitChildren(node);
    }

    @Override
    public void visit(Parameter node) {
        if (matchesBugLocation(node)) {
            String name = node.getName().getName();
            boolean validParametername = false;

            if (currentParameterDefinitions != null) {
                for (ParameterDefinition def : currentParameterDefinitions) {
                    if (name.equals(def.getIdent().getName())) {
                        validParametername = true;
                        break;
                    }
                }
            }

            if (validParametername) {
                addFix(node, node.getMetadata());
            }
        }
        visitChildren(node);
    }
}
