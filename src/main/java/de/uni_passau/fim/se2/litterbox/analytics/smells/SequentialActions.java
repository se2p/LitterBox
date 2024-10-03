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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.RepeatedSubsequenceFinder;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SequenceToLoop;

import java.util.*;

public class SequentialActions extends AbstractIssueFinder {

    @Override
    public void visit(StmtList statementList) {
        new RepeatedSubsequenceFinder() {
            @Override
            protected void handleRepetition(StmtList stmtList, List<Stmt> subsequence, int occurrences) {
                IssueBuilder builder = prepareIssueBuilder().withCurrentNode(subsequence.get(0))
                                .withMetadata(subsequence.get(0).getMetadata())
                                .withSeverity(IssueSeverity.LOW)
                                .withHint(new Hint(getName()));
                SequenceToLoop sequenceToLoop = new SequenceToLoop(stmtList, subsequence, occurrences);
                ScriptEntity refactoring = sequenceToLoop.apply(getCurrentScriptEntity());
                addIssue(builder.withRefactoring(refactoring));
            }
        }.findRepetitions(statementList);

        visitChildren(statementList);
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return "sequential_actions";
    }
}
