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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if the "ask and wait" block is used but the "answer" expression is never used in the same sprite.
 */
public class UnusedAnswer extends AbstractIssueFinder {

    public static final String NAME = "unused_answer";

    private List<AskAndWait> askBlocks;
    private boolean answerUsed;

    @Override
    public void visit(ActorDefinition node) {
        askBlocks = new ArrayList<>();
        answerUsed = false;
        super.visit(node);

        if (!askBlocks.isEmpty() && !answerUsed) {
            for (AskAndWait ask : askBlocks) {
                addIssue(ask, ask.getMetadata(), IssueSeverity.LOW, Hint.fromKey(getName()));
            }
        }
    }

    @Override
    public void visit(AskAndWait node) {
        askBlocks.add(node);
        visitChildren(node);
    }

    @Override
    public void visit(Answer node) {
        answerUsed = true;
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
