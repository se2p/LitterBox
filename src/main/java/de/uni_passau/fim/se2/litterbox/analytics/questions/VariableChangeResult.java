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

import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.ScratchBlocksFormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a question asking for the result of a variable change (set or change by).
 *
 */
public class VariableChangeResult extends AbstractQuestionFinder {

    @Override
    public void visit(SetVariableTo node) {
        String varName = getVariableName(node.getIdentifier());
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getExpr()));
        if ("true".equalsIgnoreCase(value)) {
            value = QuestionText.TRUE_VALUE;
        } else if ("false".equalsIgnoreCase(value)) {
            value = QuestionText.FALSE_VALUE;
        }

        List<TranslatableText> distractors = generateDistractorsForValue(value);
        List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(
                Collections.singletonList(QuestionText.fromText(value)), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.VARIABLE, varName)
                .withTranslatableChoices(options)
                .withTranslatableCorrectAnswers(Collections.singletonList(QuestionText.fromText(value)));
        addQuestion(builder);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        String varName = getVariableName(node.getIdentifier());
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getExpr()));
        // ...

        TranslatableText changeDesc;
        List<TranslatableText> distractors = new ArrayList<>();

        if (value.startsWith("-")) {
            String absVal = value.substring(1);
            changeDesc = QuestionText.fromKey("it_decreases_by").withQuestionParameter("VALUE", absVal);
            distractors.add(QuestionText.fromKey("it_increases_by").withQuestionParameter("VALUE", absVal));
            distractors.add(QuestionText.fromKey("it_sets_the_value_to").withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_value_to")
                    .withQuestionParameter("VALUE", absVal));
        } else {
            changeDesc = QuestionText.fromKey("it_increases_by").withQuestionParameter("VALUE", value);
            distractors.add(QuestionText.fromKey("it_decreases_by").withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_value_to").withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_value_to")
                    .withQuestionParameter("VALUE", "-" + value));
        }

        distractors.add(QuestionText.fromKey("it_does_not_change"));

        List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(
                Collections.singletonList(changeDesc), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.VARIABLE, varName)
                .withTranslatableChoices(options)
                .withTranslatableCorrectAnswers(Collections.singletonList(changeDesc));
        addQuestion(builder);
    }

    private String getVariableName(Identifier id) {
        if (id instanceof LocalIdentifier localId) {
            return localId.getName();
        }
        return ScratchBlocksVisitor.of(id);
    }

    private List<TranslatableText> generateDistractorsForValue(String value) {
        List<TranslatableText> distractors = new ArrayList<>();
        try {
            double num = Double.parseDouble(value);
            distractors.add(QuestionText.fromText(String.valueOf(num + 1)));
            distractors.add(QuestionText.fromText(String.valueOf(num - 1)));
            distractors.add(QuestionText.fromText(String.valueOf(num * 2)));
            distractors.add(QuestionText.fromText("0"));
            distractors.add(QuestionText.fromText(String.valueOf(-num)));
        } catch (NumberFormatException e) {
            // Not a number
            if (QuestionText.TRUE_VALUE.equals(value)) {
                distractors.add(QuestionText.fromKey(QuestionText.FALSE_VALUE));
            } else if (QuestionText.FALSE_VALUE.equals(value)) {
                distractors.add(QuestionText.fromKey(QuestionText.TRUE_VALUE));
            } else {
                distractors.add(QuestionText.fromText("0"));
                distractors.add(QuestionText.fromText("1"));
                distractors.add(QuestionText.fromText(""));
                distractors.add(QuestionText.fromText(value + "1"));
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
