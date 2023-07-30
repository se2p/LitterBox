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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

/**
 * Checks if there are used variables.
 */
public class UsedVariables extends AbstractIssueFinder {

    public static final String NAME = "used_variables";
    public static final String NAME_LIST = "used_variables_list";

    private boolean insideProcedure;
    private boolean insideScript;
    private boolean insideQualified;
    private Map<String, VariableInfo> varMap;
    private Map<String, ExpressionListInfo> listMap;

    private List<String> flaggedVariables;
    private List<String> flaggedLists;

    private String actorName;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        varMap = program.getSymbolTable().getVariables();
        listMap = program.getSymbolTable().getLists();
        flaggedVariables = new ArrayList<>();
        flaggedLists = new ArrayList<>();
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        super.visit(node);
        insideScript = false;
    }

    @Override
    public void visit(Qualified node) {
        if (insideProcedure || insideScript) {
            insideQualified = true;
            actorName = node.getFirst().getName();
            visitChildren(node);
            insideQualified = false;
        }
    }

    @Override
    public void visit(Variable node) {
        if (insideQualified && !flaggedVariables.contains(actorName + node.getName().getName())) {
            for (Map.Entry<String, VariableInfo> entry : varMap.entrySet()) {
                VariableInfo curr = entry.getValue();
                String actorNameInfo = curr.getActor();
                String variableNameInfo = curr.getVariableName();
                if (actorNameInfo.equals(actorName)
                        && variableNameInfo.equals(node.getName().getName())) {
                    Hint hint = new Hint(NAME);
                    addIssue(node, node.getMetadata(), hint);
                    flaggedVariables.add(actorName + node.getName().getName());
                    break;
                }
            }
        }
    }

    @Override
    public void visit(ScratchList node) {
        if (insideQualified && !flaggedLists.contains(actorName + node.getName().getName())) {
            for (Map.Entry<String, ExpressionListInfo> entry : listMap.entrySet()) {
                ExpressionListInfo curr = entry.getValue();
                String actorNameInfo = curr.getActor();
                String listNameInfo = curr.getVariableName();
                if (actorNameInfo.equals(actorName)
                        && listNameInfo.equals(node.getName().getName())) {
                    Hint hint = new Hint(NAME_LIST);
                    addIssue(node, node.getMetadata(), hint);
                    flaggedLists.add(actorName + node.getName().getName());
                    break;
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.isSubsumedBy(first, other);
        }

        if (!(other.getFinder() instanceof MissingInitialization)) {
            return false;
        }

        return other.getCodeLocation() == getParentStmt(first.getCodeLocation());
    }

    private Stmt getParentStmt(ASTNode node) {
        while (!(node instanceof Stmt)) {
            node = node.getParentNode();
        }
        return (Stmt) node;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(NAME_LIST);
        return keys;
    }
}
