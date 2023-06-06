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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfElseToDisjunction;

/*
If A:
  B
Else:
  If C:
    B

to

If A || C:
  B
 */
public class IfElseToDisjunctionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        StmtList elseStmts = ifElseStmt.getElseStmts();
        if (elseStmts.getNumberOfStatements() == 1) {
            Stmt elseStmt = elseStmts.getStatement(0);
            if (elseStmt instanceof IfThenStmt ifThenStmt
                    && ifElseStmt.getThenStmts().equals(ifThenStmt.getThenStmts())) {
                refactorings.add(new IfElseToDisjunction(ifElseStmt, ifThenStmt));
            }
        }

        visitChildren(ifElseStmt);
    }

    @Override
    public String getName() {
        return IfElseToDisjunction.NAME;
    }
}
