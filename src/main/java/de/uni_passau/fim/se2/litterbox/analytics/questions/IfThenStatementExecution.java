package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

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
            Stmt statement = thenStmts.getStatement(random.nextInt(thenStmts.getNumberOfStatements()));

            IssueBuilder builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.STATEMENT, statement.getScratchBlocks());
            hint.setParameter(Hint.CONDITION, condition.getScratchBlocks());
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
