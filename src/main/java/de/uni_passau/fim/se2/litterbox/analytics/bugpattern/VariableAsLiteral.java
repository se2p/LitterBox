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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

public class VariableAsLiteral extends AbstractIssueFinder {

    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;
    private Set<String> variablesInScope = new LinkedHashSet<>();
    private Stmt currentStatement;
    private Metadata currentMetadata;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        variablesInScope = new LinkedHashSet<>();
        currentMetadata = null;
        currentStatement = null;
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(StringLiteral node) {
        if (currentScript == null && currentProcedure == null) {
            return;
        }

        String literal = node.getText();
        if (variablesInScope.contains(literal)) {
            addIssue(currentStatement, currentMetadata);
        }
    }

    @Override
    public void visit(Variable node) {
        // No-op
    }

    @Override
    public void visit(Parameter node) {
        // No-op
    }

    @Override
    public void visit(Message node) {
        // No-op
    }

    @Override
    public void visit(Stmt node) {
        currentStatement = node;
        super.visit(node);
    }

    @Override
    public void visit(Metadata node) {
        currentMetadata = node;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;

        // Only visit statements of procedure
        visit(node.getStmtList());

        currentProcedure = null;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());

        variablesInScope = varMap.values().stream().filter(v -> v.isGlobal() || v.getActor().equals(currentActor.getIdent().getName())).map(VariableInfo::getVariableName).collect(Collectors.toSet());
        variablesInScope.addAll(listMap.values().stream().filter(v -> v.isGlobal() || v.getActor().equals(currentActor.getIdent().getName())).map(ExpressionListInfo::getVariableName).collect(Collectors.toSet()));
        if (procMap != null) {
            for (ProcedureInfo procInfo : procMap.values()) {
                for (ArgumentInfo ai : procInfo.getArguments()) {
                    variablesInScope.add(ai.getName());
                }
            }
        }
        actor.getScripts().accept(this);
        actor.getProcedureDefinitionList().accept(this);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return "variable_as_literal";
    }
}
