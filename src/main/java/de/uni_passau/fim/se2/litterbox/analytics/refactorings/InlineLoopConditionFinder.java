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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.InlineLoopCondition;

public class InlineLoopConditionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Only refactor connected blocks
            return;
        }
        if (script.getStmtList().getNumberOfStatements() == 0) {
            return;
        }
        Stmt lastStmt = script.getStmtList().getStatement(script.getStmtList().getNumberOfStatements() - 1);
        if (lastStmt instanceof UntilStmt untilStmt) {
            // TODO: Could also cover the case where it is followed by a TerminationStmt
            refactorings.add(new InlineLoopCondition(untilStmt));
        }
    }

    @Override
    public String getName() {
        return InlineLoopCondition.NAME;
    }
}
