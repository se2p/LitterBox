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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

import java.util.List;

/**
 * Checks for an If-/IfElse block inside of a loop.
 */
public class ConditionalInLoop extends AbstractIssueFinder {

    public static final String NAME = "conditional_in_loop";

    @Override
    public void visit(RepeatForeverStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        hasNested(node.getStmtList().getStmts());
        visitChildren(node);
    }

    private void hasNested(List<Stmt> stmtList) {
        for (Stmt stmt : stmtList) {
            if (stmt instanceof IfStmt) {
                addIssue(stmt, stmt.getMetadata(), IssueSeverity.MEDIUM);
            }
        }
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
