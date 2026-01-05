/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingAsk;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Set;

/**
 * Fix heuristic for the MissingAsk bug.
 *
 * <p>The bug is reported when the Answer block is used but Ask is never called.
 * The bug is fixed if the program now also uses AskAndWait.
 */
public class MissingAskFix extends AbstractFixHeuristic {

    private boolean askUsed;
    private Answer targetAnswer;

    private final MissingAsk relatedBugFinder = new MissingAsk();

    public MissingAskFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        askUsed = false;
        targetAnswer = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(AskAndWait node) {
        askUsed = true;
        visitChildren(node);
    }

    @Override
    public void visit(Answer node) {
        if (matchesBugLocation(node)) {
            targetAnswer = node;
        }
        visitChildren(node);
    }

    @Override
    public Set<FixResult> check(Program program) {
        super.check(program);

        if (targetAnswer != null && askUsed) {
            // Fixed: The answer block exists, and Ask IS used now.
            addFix(targetAnswer, targetAnswer.getMetadata());
        }
        return java.util.Collections.unmodifiableSet(fixes);
    }
}
