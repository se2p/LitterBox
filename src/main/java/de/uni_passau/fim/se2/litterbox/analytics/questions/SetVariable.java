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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

/**
 * Generates a question asking for the value a variable is set to.
 *
 */
public class SetVariable extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "set_variable";
    private static final String VARIABLE_PARAM = "VARIABLE";
    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";
    private static final String OPEN_BRACKET = "[";
    private static final String CLOSE_BRACKET = "]";

    private boolean inScript;

    @Override
    public void visit(Script node) {
        inScript = true;
        super.visit(node);
        inScript = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (inScript) {
            Identifier identifier = node.getIdentifier();
            Expression expression = node.getExpr();

            if (expression instanceof NumberLiteral || expression instanceof StringLiteral) {
                QuestionBuilder builder = prepareQuestionBuilder(node)
                        .withQuestionParameter(VARIABLE_PARAM, ScratchBlocksVisitor.of(identifier))
                        .withCorrectAnswers(getExpressionValue(expression));
                addQuestion(builder);
            }
        }
    }

    /**
     * Returns expression value without open- and close-parentheses/brackets.
     *
     * @param node Expression node
     * @return expression value without wrapper characters
     */
    private String getExpressionValue(Expression node) {
        String value = ScratchBlocksVisitor.of(node).trim();
        // Strip outer parentheses (for numbers) or brackets (for strings)
        if ((value.startsWith(OPEN_PAREN) && value.endsWith(CLOSE_PAREN))
                || (value.startsWith(OPEN_BRACKET) && value.endsWith(CLOSE_BRACKET))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
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
        return ScopeOfFocus.ATOM;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.FREE_TEXT;
    }
}
