/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * When custom blocks are created the user can define parameters, which can then be used in the body of the custom
 * block. The program only works as intended as long as the parameter is initialized. This is the solution pattern for
 * the bug pattern "Orphaned Parameter".
 */
public class MatchingParameter extends AbstractIssueFinder {
    public static final String NAME = "matching_parameter";
    private List<ParameterDefinition> currentParameterDefinitions;
    private List<String> checkedList;

    @Override
    public void visit(ActorDefinition actor) {
        currentParameterDefinitions = new ArrayList<>();
        checkedList = new ArrayList<>();
        super.visit(actor);
    }

    @Override
    public void visit(Script node) {
        //NOP should not be detected in Scripts
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentParameterDefinitions = node.getParameterDefinitionList().getParameterDefinitions();
        checkedList = new ArrayList<>();
        super.visit(node);
    }

    @Override
    public void visit(Parameter node) {
        if (!checked(node.getName().getName())) {
            checkParameterNames(node.getName().getName(), node);
            checkedList.add(node.getName().getName());
        }
        visitChildren(node);
    }

    private void checkParameterNames(String name, Parameter node) {
        for (ParameterDefinition parameterDefinition : currentParameterDefinitions) {
            if (name.equals(parameterDefinition.getIdent().getName())) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                break;
            }
        }
    }

    private boolean checked(String name) {
        for (String parameter : checkedList) {
            if (parameter.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
