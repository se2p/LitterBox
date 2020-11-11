/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if there are any unused parameters in one of the custom blocks.
 */
public class UnusedParameter extends AbstractIssueFinder {
    public static final String NAME = "unused_parameter";
    private boolean insideProcedure;
    private List<String> usedParameterNames;

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        usedParameterNames = new ArrayList<>();
        super.visit(node);
        List<ParameterDefinition> parameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        for (ParameterDefinition def : parameterDefinitions) {
            if (!usedParameterNames.contains(def.getIdent().getName())) {
                addIssue(def, def.getMetadata());
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(Parameter node) {
        if (insideProcedure) {
            usedParameterNames.add(node.getName().getName());
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
