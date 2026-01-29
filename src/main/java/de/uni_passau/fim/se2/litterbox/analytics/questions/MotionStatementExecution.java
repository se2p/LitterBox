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

import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
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
public class MotionStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(ChangeXBy node) {
        String value = cleanValue(ScratchBlocksVisitor.of(node.getNum()));
        generateChangeQuestion("x position", value, node);
    }

    @Override
    public void visit(ChangeYBy node) {
        String value = cleanValue(ScratchBlocksVisitor.of(node.getNum()));
        generateChangeQuestion("y position", value, node);
    }

    @Override
    public void visit(SetXTo node) {
        String value = cleanValue(ScratchBlocksVisitor.of(node.getNum()));
        generateSetQuestion("x position", value, node);
    }

    @Override
    public void visit(SetYTo node) {
        String value = cleanValue(ScratchBlocksVisitor.of(node.getNum()));
        generateSetQuestion("y position", value, node);
    }

    private String cleanValue(String val) {
        if (val.startsWith("(") && val.endsWith(")")) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    private void generateChangeQuestion(String attribute, String value, de.uni_passau.fim.se2.litterbox.ast.model.ASTNode node) {
        String changeDesc;
        List<String> distractors = new ArrayList<>();

        if (value.startsWith("-")) {
            String absVal = value.substring(1);
            changeDesc = "It decreases by " + absVal;
            distractors.add("It increases by " + absVal);
            distractors.add("It sets the " + attribute + " to " + value);
            distractors.add("It sets the " + attribute + " to " + absVal);
        } else {
            changeDesc = "It increases by " + value;
            distractors.add("It decreases by " + value);
            distractors.add("It sets the " + attribute + " to " + value);
            distractors.add("It sets the " + attribute + " to -" + value);
        }

        distractors.add("It does not change");

        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(changeDesc), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.ATTRIBUTE, attribute)
                .withChoices(options)
                .withCorrectAnswers(Collections.singletonList(changeDesc));
        addQuestion(builder);
    }

    private void generateSetQuestion(String attribute, String value, de.uni_passau.fim.se2.litterbox.ast.model.ASTNode node) {
        List<String> distractors = generateDistractorsForValue(value);
        List<String> options = generateMultipleChoiceOptions(Collections.singletonList(value), distractors);

        QuestionBuilder builder = prepareQuestionBuilder(node)
                .withQuestionText(QuestionText.fromKey(getName()))
                .withQuestionParameter(QuestionText.ATTRIBUTE, attribute)
                .withChoices(options)
                .withCorrectAnswers(Collections.singletonList(value));
        addQuestion(builder);
    }

    private List<String> generateDistractorsForValue(String value) {
        List<String> distractors = new ArrayList<>();
        try {
            double num = Double.parseDouble(value);
            distractors.add(NumberFormatUtil.format(num + 10)); // Motion usually involves larger steps
            distractors.add(NumberFormatUtil.format(num - 10));
            distractors.add(NumberFormatUtil.format(num * 2));
            distractors.add("0");
            distractors.add(NumberFormatUtil.format(-num));
        } catch (NumberFormatException e) {
            distractors.add("0");
            distractors.add("100");
            distractors.add("-100");
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

    @Override
    public String getName() {
        return "motion_statement_execution";
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
