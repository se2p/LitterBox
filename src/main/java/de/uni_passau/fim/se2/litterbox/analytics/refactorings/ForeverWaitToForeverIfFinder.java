/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ForeverWaitToForeverIf;

public class ForeverWaitToForeverIfFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt loop) {
        if (loop.getStmtList().getNumberOfStatements() > 1) {
            Stmt initialStatement = loop.getStmtList().getStatement(0);
            if (initialStatement instanceof WaitUntil) {
                refactorings.add(new ForeverWaitToForeverIf(loop));
            }
        }
        visitChildren(loop);
    }

    @Override
    public String getName() {
        return ForeverWaitToForeverIf.NAME;
    }
}
