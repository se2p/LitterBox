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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.List;

/**
 * Checks for nested loops.
 */
public class NestedLoops extends AbstractIssueFinder {

    public static final String NAME = "nested_loops";

    @Override
    public void visit(UntilStmt node) {
        if (checkNested(node.getStmtList().getStmts())) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (checkNested(node.getStmtList().getStmts())) {
            addIssue(node, node.getMetadata());
        }
        visitChildren(node);
    }

    private boolean checkNested(List<Stmt> stmts) {
        return stmts.size() == 1 && ((stmts.get(0) instanceof UntilStmt) || (stmts.get(0) instanceof RepeatTimesStmt) || (stmts.get(0) instanceof RepeatForeverStmt));
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (checkNested(node.getStmtList().getStmts())) {
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
