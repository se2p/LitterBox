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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.Random;

/**
 * @QuestionType Yes or No
 * @Highlighted IfThen Statement
 * @Context Single script
 */
public class IfThenStatementExecution extends AbstractQuestionFinder {

    @Override
    public void visit(IfThenStmt node) {
        if (node.getThenStmts().hasStatements()) {
            Random random = new Random();
            boolean conditionValue = random.nextBoolean();
            BoolExpr condition = node.getBoolExpr();
            StmtList thenStmts = node.getThenStmts();
            Stmt statement = getSingleStmt(thenStmts.getStatement(random.nextInt(thenStmts.getNumberOfStatements())));

            IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
            Hint hint = Hint.fromKey(getName());
            hint.setParameter(Hint.STATEMENT, ScratchBlocksVisitor.of(statement));
            hint.setParameter(Hint.CONDITION, ScratchBlocksVisitor.of(condition));
            hint.setParameter(Hint.HINT_VARIABLE, String.valueOf(conditionValue).toUpperCase());
            hint.setParameter(Hint.ANSWER, conditionValue ? YES : NO);
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public String getName() {
        return "if_then_statement_execution";
    }
}
