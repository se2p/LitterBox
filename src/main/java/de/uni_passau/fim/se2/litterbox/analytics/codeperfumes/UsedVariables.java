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
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ListContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfVar;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
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
    private Collection<VariableInfo> variables;
    private Collection<ExpressionListInfo> lists;

    private Set<String> flaggedVariables;
    private Set<String> flaggedLists;

    private String actorName;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        variables = program.getSymbolTable().getVariables().values();
        lists = program.getSymbolTable().getLists().values();
        flaggedVariables = new LinkedHashSet<>();
        flaggedLists = new LinkedHashSet<>();
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

    private void addHintToFlagableIdentifier(ASTNode node, Identifier identifier, String hintName){
        if ((insideProcedure || insideScript) && checkIdentifierForFlag(identifier)) {
            Hint hint = new Hint(hintName);
            addIssue(node, node.getMetadata(), hint);
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME);
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME);
        node.getExpr().accept(this);
    }

    @Override
    public void visit(AddTo node) {
        node.getString().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(DeleteOf node) {
        node.getNum().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(DeleteAllOf node) {
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(InsertAt node) {
        node.getString().accept(this);
        node.getIndex().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(ReplaceItem node) {
        node.getIndex().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
        node.getString().accept(this);
    }

    @Override
    public void visit(ItemOfVariable node) {
        node.getNum().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(IndexOf node) {
        node.getExpr().accept(this);
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(LengthOfVar node) {
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
    }

    @Override
    public void visit(ListContains node) {
        addHintToFlagableIdentifier(node, node.getIdentifier(), NAME_LIST);
        node.getElement().accept(this);
    }

    private boolean checkIdentifierForFlag(Identifier identifier) {
        if (identifier instanceof Qualified qualified) {
            actorName = qualified.getFirst().getName();

            if (qualified.getSecond() instanceof Variable variable) {
                if (!flaggedVariables.contains(actorName + variable.getName().getName())) {
                    return flagVariable(variable);
                }
            } else if (qualified.getSecond() instanceof ScratchList list) {
                if (!flaggedVariables.contains(actorName + list.getName().getName())) {
                    return flagList(list);
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean flagVariable(Variable variable) {
        for (VariableInfo curr : variables) {
            String actorNameInfo = curr.getActor();
            String variableNameInfo = curr.getVariableName();
            if (actorNameInfo.equals(actorName)
                    && variableNameInfo.equals(variable.getName().getName())) {
                flaggedVariables.add(actorName + variable.getName().getName());
                return true;
            }
        }
        return false;
    }

    private boolean flagList(ScratchList list) {
        for (ExpressionListInfo curr : lists) {
            String actorNameInfo = curr.getActor();
            String listNameInfo = curr.getVariableName();
            if (actorNameInfo.equals(actorName)
                    && listNameInfo.equals(list.getName().getName())) {
                flaggedLists.add(actorName + list.getName().getName());
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Variable node) {
        if (insideQualified && !flaggedVariables.contains(actorName + node.getName().getName())) {
            if (flagVariable(node)) {
                Hint hint = new Hint(NAME);
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(ScratchList node) {
        if (insideQualified && !flaggedLists.contains(actorName + node.getName().getName())) {
            if (flagList(node)) {
                Hint hint = new Hint(NAME_LIST);
                addIssue(node, node.getMetadata(), hint);
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

        return other.getCodeLocation() == AstNodeUtil.findParent(first.getCodeLocation(), Stmt.class);
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(NAME_LIST);
        return keys;
    }
}
