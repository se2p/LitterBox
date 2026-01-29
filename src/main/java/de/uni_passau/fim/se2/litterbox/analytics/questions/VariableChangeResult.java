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

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@link AbstractQuestionFinder#maxChoices}
 * @NumChoices {@link AbstractQuestionFinder#maxChoices}
 * @Highlighted Block
 * @Context Atom
 */
public class VariableChangeResult extends AbstractQuestionFinder {

    @Override
    public void visit(SetVariableTo node) {
        String varName = getVariableName(node.getIdentifier());
        String value = ScratchBlocksVisitor.of(node.getExpr());

        List<String> distractors = generateDistractorsForValue(value);
        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(value), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.VARIABLE, varName)
                .withChoices(options)
                .withCorrectAnswers(Collections.singletonList(value));
        addQuestion(builder);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        String varName = getVariableName(node.getIdentifier());
        String value = ScratchBlocksVisitor.of(node.getExpr());

        String changeDesc;
        List<String> distractors = new ArrayList<>();

        if (value.startsWith("-")) {
            String absVal = value.substring(1);
            changeDesc = "It decreases by " + absVal;
            distractors.add("It increases by " + absVal);
            distractors.add("It sets the value to " + value);
            distractors.add("It sets the value to " + absVal);
        } else {
            changeDesc = "It increases by " + value;
            distractors.add("It decreases by " + value);
            distractors.add("It sets the value to " + value);
            distractors.add("It sets the value to -" + value);
        }

        distractors.add("It does not change");

        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(changeDesc), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.VARIABLE, varName)
                .withChoices(options)
                .withCorrectAnswers(Collections.singletonList(changeDesc));
        addQuestion(builder);
    }

    private String getVariableName(Identifier id) {
        if (id instanceof LocalIdentifier localId) {
            return localId.getName();
        }
        return ScratchBlocksVisitor.of(id);
    }

    private List<String> generateDistractorsForValue(String value) {
        List<String> distractors = new ArrayList<>();
        try {
            double num = Double.parseDouble(value);
            distractors.add(String.valueOf(num + 1));
            distractors.add(String.valueOf(num - 1));
            distractors.add(String.valueOf(num * 2));
            distractors.add("0");
            distractors.add(String.valueOf(-num));
        } catch (NumberFormatException e) {
            // Not a number
            if ("true".equalsIgnoreCase(value)) {
                distractors.add("false");
            } else if ("false".equalsIgnoreCase(value)) {
                distractors.add("true");
            } else {
                distractors.add("0");
                distractors.add("1");
                distractors.add("");
                distractors.add(value + "1");
            }
        }
        return distractors;
    }

    @Override
    public String getName() {
        return "variable_change_result";
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.EXECUTION;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.ATOM;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
