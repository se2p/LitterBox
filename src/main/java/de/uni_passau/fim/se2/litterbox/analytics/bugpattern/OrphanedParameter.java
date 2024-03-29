/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.List;

/**
 * When custom blocks are created the user can define parameters, which can then be used in the body of the custom
 * block.
 * However, the block definition can be altered, including removal of parameters even if they are in use.
 * Any instances of deleted parameters are retained, and then evaluated with the standard value for the type of
 * parameter, since they are never initialised.
 */
public class OrphanedParameter extends AbstractIssueFinder {
    public static final String NAME = "orphaned_parameter";
    private List<ParameterDefinition> currentParameterDefinitions;

    @Override
    public void visit(ProcedureDefinition node) {
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        //NOP should not check Scripts
    }

    @Override
    public void visit(Parameter node) {
        checkParameterNames(node.getName().getName(), node);
        visitChildren(node);
    }

    private void checkParameterNames(String name, Parameter node) {
        boolean validParametername = false;
        for (int i = 0; i < currentParameterDefinitions.size() && !validParametername; i++) {
            if (name.equals(currentParameterDefinitions.get(i).getIdent().getName())) {
                validParametername = true;
            }
        }
        if (!validParametername) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
