package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;

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
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CONDITION, condition.getScratchBlocksWithoutNewline());
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
