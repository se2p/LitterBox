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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a question asking to evaluate a compound logic expression (AND/OR).
 *
 */
public class CompoundLogicEvaluation extends AbstractQuestionFinder {

    private static final String AND_OPERATOR_KEY = "and_operator";
    private static final String OR_OPERATOR_KEY = "or_operator";
    private static final String BINARY_EXPRESSION_KEY = "binary_expression";
    private static final String OP1_PARAM = "OP1";
    private static final String OP2_PARAM = "OP2";
    private static final String OPERATOR_PARAM = "OPERATOR";

    public static final String FINDER_NAME = "compound_logic_evaluation";

    @Override
    public void visit(IfThenStmt node) {
        checkCondition(node.getBoolExpr(), node);
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        checkCondition(node.getBoolExpr(), node);
        visitChildren(node);
    }

    private void checkCondition(BoolExpr cond, ASTNode node) {
        if (cond instanceof And || cond instanceof Or) {
            BinaryExpression<?, ?> bin = (BinaryExpression<?, ?>) cond;
            String op1 = wrappedScratchBlocks((Expression) bin.getOperand1());
            String op2 = wrappedScratchBlocks((Expression) bin.getOperand2());

            String operatorKey = (cond instanceof And) ? AND_OPERATOR_KEY : OR_OPERATOR_KEY;
            QuestionText correct = QuestionText.fromKey(BINARY_EXPRESSION_KEY)
                    .withQuestionParameter(OP1_PARAM, op1)
                    .withQuestionParameter(OPERATOR_PARAM, QuestionText.fromKey(operatorKey))
                    .withQuestionParameter(OP2_PARAM, op2);

            // Distractors
            List<TranslatableText> distractors = new ArrayList<>();
            // Distractor 1: Just first operand
            distractors.add(QuestionText.fromText(op1));
            // Distractor v2: Just second operand
            distractors.add(QuestionText.fromText(op2));
            // Distractor 3: Wrong operator
            String wrongOpKey = (cond instanceof And) ? OR_OPERATOR_KEY : AND_OPERATOR_KEY;
            distractors.add(QuestionText.fromKey(BINARY_EXPRESSION_KEY)
                    .withQuestionParameter(OP1_PARAM, op1)
                    .withQuestionParameter(OPERATOR_PARAM, QuestionText.fromKey(wrongOpKey))
                    .withQuestionParameter(OP2_PARAM, op2));

            List<TranslatableText> options = generateTranslatableMultipleChoiceOptions(
                    Collections.singletonList(correct), distractors);

            QuestionBuilder builder = prepareQuestionBuilder(node)
                    .withQuestionText(QuestionText.fromKey(getName()))
                    .withTranslatableCorrectAnswers(Collections.singletonList(correct))
                    .withTranslatableChoices(options);

            addQuestion(builder);
        }
    }

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
        return ScopeOfFocus.BLOCK;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
