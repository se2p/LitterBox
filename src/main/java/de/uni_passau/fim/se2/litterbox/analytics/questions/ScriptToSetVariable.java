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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Nothing
 * @Context Whole program
 */
public class ScriptToSetVariable extends AbstractQuestionFinder {
    private String variable;
    private Map<String, String> answers;

    @Override
    public void visit(Program node) {
        answers = new HashMap<>();
        super.visit(node);

        currentScript = null;
        currentProcedure = null;

        if (!choices.isEmpty() && !answers.isEmpty()) {
            answers.forEach((variable, answer) -> {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.HINT_VARIABLE, variable);
                hint.setParameter(Hint.ANSWER, answer);
                hint.setParameter(Hint.CHOICES, getChoices());
                addIssue(builder.withHint(hint));
            });
        }
    }

    @Override
    public void visit(Script node) {
        variable = null;
        super.visit(node);
        if (!isNull(variable)) {
            answers.putIfAbsent(variable, wrappedScratchBlocks(node));
        } else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        variable = ScratchBlocksVisitor.of(node.getIdentifier());
    }

    @Override
    public String getName() {
        return "script_to_set_variable";
    }
}
