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

import de.uni_passau.fim.se2.litterbox.analytics.HintPlaceholder;
import de.uni_passau.fim.se2.litterbox.analytics.TranslatableText;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.ScratchBlocksFormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a question asking about the effect of a motion statement (e.g., change x by 10).
 *
 */
public class MotionStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(ChangeXBy node) {
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getNum()));
        generateChangeQuestion("x_position_attr", value, node);
    }

    @Override
    public void visit(ChangeYBy node) {
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getNum()));
        generateChangeQuestion("y_position_attr", value, node);
    }

    @Override
    public void visit(SetXTo node) {
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getNum()));
        generateSetQuestion("x_position_attr", value, node);
    }

    @Override
    public void visit(SetYTo node) {
        String value = ScratchBlocksFormatUtil.stripParentheses(ScratchBlocksVisitor.of(node.getNum()));
        generateSetQuestion("y_position_attr", value, node);
    }

    private void generateChangeQuestion(String attributeKey, String value, ASTNode node) {
        TranslatableText changeDesc;
        List<TranslatableText> distractors = new ArrayList<>();

        if (value.startsWith("-")) {
            String absVal = value.substring(1);
            changeDesc = QuestionText.fromKey("it_decreases_by").withQuestionParameter("VALUE", absVal);
            distractors.add(QuestionText.fromKey("it_increases_by").withQuestionParameter("VALUE", absVal));
            distractors.add(QuestionText.fromKey("it_sets_the_attribute_to")
                    .withQuestionParameter("ATTRIBUTE", attributeKey).withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_attribute_to")
                    .withQuestionParameter("ATTRIBUTE", attributeKey).withQuestionParameter("VALUE", absVal));
        } else {
            changeDesc = QuestionText.fromKey("it_increases_by").withQuestionParameter("VALUE", value);
            distractors.add(QuestionText.fromKey("it_decreases_by").withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_attribute_to")
                    .withQuestionParameter("ATTRIBUTE", attributeKey).withQuestionParameter("VALUE", value));
            distractors.add(QuestionText.fromKey("it_sets_the_attribute_to")
                    .withQuestionParameter("ATTRIBUTE", attributeKey).withQuestionParameter("VALUE", "-" + value));
        }

        distractors.add(QuestionText.fromKey("it_does_not_change"));

        List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(
                Collections.singletonList(changeDesc), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionType(QuestionType.MULTIPLE_CHOICE)
                .withQuestionParameter("ATTRIBUTE", QuestionText.fromKey(attributeKey))
                .withTranslatableChoices(options)
                .withTranslatableCorrectAnswers(Collections.singletonList(changeDesc));
        addQuestion(builder);
    }

    private void generateSetQuestion(String attribute, String value, ASTNode node) {
        List<TranslatableText> distractors = generateDistractorsForValue(value);
        List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(
                Collections.singletonList(QuestionText.fromText(value)), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.ATTRIBUTE, new HintPlaceholder.Translatable(attribute))
                .withTranslatableChoices(options)
                .withTranslatableCorrectAnswers(Collections.singletonList(QuestionText.fromText(value)));
        addQuestion(builder);
    }

    private List<TranslatableText> generateDistractorsForValue(String value) {
        List<TranslatableText> distractors = new ArrayList<>();
        try {
            double num = Double.parseDouble(value);
            distractors.add(QuestionText.fromText(NumberFormatUtil.format(num + 10)));
            // Motion usually involves larger steps
            distractors.add(QuestionText.fromText(NumberFormatUtil.format(num - 10)));
            distractors.add(QuestionText.fromText(NumberFormatUtil.format(num * 2)));
            distractors.add(QuestionText.fromText("0"));
            distractors.add(QuestionText.fromText(NumberFormatUtil.format(-num)));
        } catch (NumberFormatException e) {
            distractors.add(QuestionText.fromText("0"));
            distractors.add(QuestionText.fromText("100"));
            distractors.add(QuestionText.fromText("-100"));
        }
        return distractors;
    }

    private static class NumberFormatUtil {
        static String format(double d) {
            if (d == (long) d) {
                return String.format("%d", (long) d);
            } else {
                return String.format("%s", d);
            }
        }
    }

    public static final String FINDER_NAME = "motion_statement_execution";

    @Override
    public String getName() {
        return FINDER_NAME;
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
