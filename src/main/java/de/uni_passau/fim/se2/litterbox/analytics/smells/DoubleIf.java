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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.List;

public class DoubleIf extends AbstractIssueFinder {

    private static final String NAME = "double_if";

    @Override
    public void visit(StmtList node) {
        final List<Stmt> stmts = node.getStmts();
        BoolExpr lastCondition = null;
        for (Stmt s : stmts) {
            if (s instanceof IfStmt) {
                BoolExpr condition = getCondition((IfStmt) s);
                if (lastCondition != null) {
                    if (lastCondition.equals(condition)) {
                        addIssue(s, getMetadata((IfStmt) s));
                    }
                }
                lastCondition = condition;
            } else {
                // even if we already have a condition from an ifstmt before, it only counts if a second ifstmt
                // follows directly after the first.
                lastCondition = null;
            }
        }

        visitChildren(node);
    }

    private BoolExpr getCondition(IfStmt s) {
        if (s instanceof IfThenStmt) {
            return ((IfThenStmt) s).getBoolExpr();
        } else if (s instanceof IfElseStmt) {
            return ((IfElseStmt) s).getBoolExpr();
        } else {
            throw new IllegalArgumentException("Cannot get condition of anything but IfStmts");
        }
    }

    private BlockMetadata getMetadata(IfStmt s) {
        if (s instanceof IfThenStmt) {
            return ((IfThenStmt) s).getMetadata();
        } else if (s instanceof IfElseStmt) {
            return ((IfElseStmt) s).getMetadata();
        } else {
            throw new IllegalArgumentException("Cannot get condition of anything but IfStmts");
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
