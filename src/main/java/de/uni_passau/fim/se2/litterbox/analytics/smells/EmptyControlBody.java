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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

/**
 * Checks for empty if or else bodies.
 */
public class EmptyControlBody extends AbstractIssueFinder {
    public static final String NAME = "empty_control_body";

    @Override
    public void visit(IfElseStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        if (node.getElseStmts().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (node.getThenStmts().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
