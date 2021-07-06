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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.RepeatedSubsequenceFinder;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SequenceToLoop;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.List;

public class SequenceToLoopFinder extends AbstractRefactoringFinder {

    private static final int MIN_LENGTH = PropertyLoader.getSystemIntProperty("refactoring.sequence_to_loop.min_length");
    private static final int MIN_OCCURRENCE = PropertyLoader.getSystemIntProperty("refactoring.sequence_to_loop.min_occurrence");


    @Override
    public void visit(StmtList statementList) {
        new RepeatedSubsequenceFinder(MIN_LENGTH, MIN_OCCURRENCE) {
            @Override
            protected void handleRepetition(StmtList stmtList, List<Stmt> subsequence, int occurrences) {
                refactorings.add(new SequenceToLoop(statementList, subsequence, occurrences));
            }
        }.findRepetitions(statementList);

        super.visit(statementList);
    }

    @Override
    public String getName() {
        return SequenceToLoop.NAME;
    }
}
