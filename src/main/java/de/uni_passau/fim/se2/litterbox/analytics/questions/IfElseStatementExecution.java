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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.Random;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices 1
 * @Highlighted IfElse Statement
 * @Context Single script
 */
public class IfElseStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(IfElseStmt node) {
        if (node.getThenStmts().hasStatements() && node.getElseStmts().hasStatements()) {
            Random random = new Random();
            boolean conditionValue = random.nextBoolean();
            BoolExpr condition = node.getBoolExpr();

            String thenStmts = wrappedScratchBlocks(node.getThenStmts());
            String elseStmts = wrappedScratchBlocks(node.getElseStmts());

            IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.CONDITION, ScratchBlocksVisitor.of(condition));
            hint.setParameter(Hint.HINT_VARIABLE, String.valueOf(conditionValue).toUpperCase());
            hint.setParameter(Hint.ANSWER, conditionValue ? thenStmts : elseStmts);
            hint.setParameter(Hint.CHOICES, conditionValue ? elseStmts : thenStmts);
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public String getName() {
        return "if_else_statement_execution";
    }
}
