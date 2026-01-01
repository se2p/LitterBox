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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

import java.util.List;
import java.util.Set;

public class IllegalParameterRefactor extends AbstractIssueFinder {
    public static final String NAME = "illegal_parameter_refactor";
    private List<ParameterDefinition> currentParameters;
    private boolean insideProcedure;

    @Override
    public void visit(AsBool node) {
        if (insideProcedure) {
            checkBool(node);
        }
        visitChildren(node);
    }

    private void checkBool(AsBool asBool) {
        if (asBool.getOperand1() instanceof Parameter ident) {
            for (ParameterDefinition param : currentParameters) {
                if (param.getIdent().getName().equals(ident.getName().getName())
                        && !(param.getType() instanceof BooleanType)) {
                    addIssue(ident, ident.getMetadata(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public Set<Issue> check(Program program) {
        return super.check(program);
    }

    @Override
    public void visit(ActorDefinition actor) {
        super.visit(actor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentParameters = node.getParameterDefinitionList().getParameterDefinitions();
        super.visit(node);
        insideProcedure = false;
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
