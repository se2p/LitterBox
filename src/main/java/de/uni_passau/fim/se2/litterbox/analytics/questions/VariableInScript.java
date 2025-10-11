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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * @QuestionType Strings
 * @NumAnswers {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Script
 * @Context Single script
 */
public class VariableInScript extends AbstractQuestionFinder {

    @Override
    public void visit(Script node) {
        answers.clear();
        super.visit(node);

        if (!answers.isEmpty()) {
            ASTNode topBlockCurrent;
            if (!(node.getEvent() instanceof Never)) {
                topBlockCurrent = node.getEvent();
            } else {
                topBlockCurrent = node.getStmtList().getStmts().getFirst();
            }
            IssueBuilder builder = prepareIssueBuilder(topBlockCurrent).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(Variable node) {
        answers.add(node.getName().getName());
    }

    @Override
    public String getName() {
        return "variable_in_script";
    }
}
