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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleIf;

import java.util.List;

/*
if A:
  B
if A:
  C

to

if A:
  B
  C
 */
public class MergeDoubleIfFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(StmtList node) {
        final List<Stmt> stmts = node.getStmts();

        for (int i = 0; i < stmts.size() - 1; i++) {
            // Only merge successive ifs with the same condition
            if (stmts.get(i) instanceof IfThenStmt
                    && stmts.get(i + 1) instanceof IfThenStmt) {
                IfThenStmt ifThenStmt1 = (IfThenStmt) stmts.get(i);
                IfThenStmt ifThenStmt2 = (IfThenStmt) stmts.get(i + 1);
                if (ifThenStmt1.getBoolExpr().equals(ifThenStmt2.getBoolExpr())) {
                    refactorings.add(new MergeDoubleIf(ifThenStmt1, ifThenStmt2));
                }
            }
        }

        visitChildren(node);
    }

    @Override
    public String getName() {
        return MergeDoubleIf.NAME;
    }
}
