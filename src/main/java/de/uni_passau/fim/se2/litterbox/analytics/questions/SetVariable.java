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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

/**
 * @QuestionType Strings
 * @NumAnswers 1
 * @Highlighted Statement
 * @Context Single script
 */
public class SetVariable extends AbstractQuestionFinder {

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
                IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.HINT_VARIABLE, ScratchBlocksVisitor.of(identifier));
                hint.setParameter(Hint.ANSWER, getExpressionValue(expression));
                addIssue(builder.withHint(hint));
            }
        }
    }

    /**
     * Returns expression value without open- and close-parentheses.
     *
     * @param node Expression node
     * @return expression value without parentheses
     */
    private String getExpressionValue(Expression node) {
        String value = ScratchBlocksVisitor.of(node);
        return value.substring(1, value.length() - 1);
    }

    @Override
    public String getName() {
        return "set_variable";
    }
}
