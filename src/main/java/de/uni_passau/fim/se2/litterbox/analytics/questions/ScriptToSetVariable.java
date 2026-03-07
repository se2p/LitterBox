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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Generates a question asking which script sets a variable to a specific value.
 *
 */
public class ScriptToSetVariable extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "script_to_set_variable";
    private static final String VARIABLE_PARAM = "VARIABLE";

    private String variable;
    private Map<String, String> variableAnswers;

    @Override
    public void visit(Program node) {
        variableAnswers = new HashMap<>();
        super.visit(node);

        currentScript = null;
        currentProcedure = null;

        if (!choices.isEmpty() && !variableAnswers.isEmpty()) {
            variableAnswers.forEach((variable, answer) -> {
                QuestionBuilder builder = prepareQuestionBuilder()
                        .withQuestionParameter(VARIABLE_PARAM, variable)
                        .withCorrectAnswers(answer)
                        .withChoices(generateMultipleChoiceOptions(Collections.singletonList(answer), choices));
                addQuestion(builder);
            });
        }
    }

    @Override
    public void visit(Script node) {
        variable = null;
        super.visit(node);
        if (!isNull(variable)) {
            variableAnswers.putIfAbsent(variable, wrappedScratchBlocks(node));
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
        return FINDER_NAME;
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.TEXT;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.RELATION;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
