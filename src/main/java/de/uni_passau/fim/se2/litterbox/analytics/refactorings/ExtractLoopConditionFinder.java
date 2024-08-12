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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ExtractLoopCondition;

public class ExtractLoopConditionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {

        for (Stmt stmt : repeatForeverStmt.getStmtList().getStmts()) {
            if (stmt instanceof IfThenStmt ifThenStmt) {
                if (ifThenStmt.getThenStmts().getNumberOfStatements() == 1
                        && ifThenStmt.getThenStmts().getStatement(0) instanceof TerminationStmt) {
                    refactorings.add(new ExtractLoopCondition(repeatForeverStmt, ifThenStmt));
                }
            }
        }

        visitChildren(repeatForeverStmt);
    }

    @Override
    public String getName() {
        return ExtractLoopCondition.NAME;
    }
}
