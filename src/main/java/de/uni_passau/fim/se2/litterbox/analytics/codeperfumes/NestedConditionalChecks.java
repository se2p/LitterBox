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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Looks for nested if/else condition blocks.
 */
public class NestedConditionalChecks extends AbstractIssueFinder {

    public static final String NAME = "nested_conditional_checks";
    private List<ASTNode> addedStmts = new ArrayList<>();

    @Override
    public void visit(ActorDefinition actor) {
        addedStmts = new ArrayList<>();
        super.visit(actor);
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode())
                && (hasNested(node.getThenStmts().getStmts()) || hasNested(node.getElseStmts().getStmts()))) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!addedStmts.contains(node.getParentNode().getParentNode()) && hasNested(node.getThenStmts().getStmts())) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            addedStmts.add(node);
        }
        visitChildren(node);
    }

    private boolean hasNested(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            if (stmt instanceof IfStmt) {
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
