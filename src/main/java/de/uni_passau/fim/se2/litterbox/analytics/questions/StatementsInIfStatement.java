package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers {@code MAX_CHOICES}
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Script
 * @Context Single script
 */
public class StatementsInIfStatement extends AbstractQuestionFinder {

    private int inIfStmt;
    private boolean visitingScript;

    @Override
    public void visit(Script node) {
        choices.clear();
        answers.clear();

        visitingScript = true;
        inIfStmt = 0;
        super.visit(node);
        visitingScript = false;
        choices.removeAll(answers);

        if (!answers.isEmpty() && !choices.isEmpty()) {
            IssueBuilder builder = prepareIssueBuilder(node.getEvent()).withSeverity(IssueSeverity.LOW);
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        inIfStmt++;
        visit(node.getThenStmts());
        inIfStmt--;
    }

    @Override
    public void visit(IfElseStmt node) {
        inIfStmt++;
        visit(node.getThenStmts());
        visit(node.getElseStmts());
        inIfStmt--;
    }

    @Override
    public void visit(LoopStmt node) {
        if (inIfStmt == 0)
            visit(node.getStmtList());
    }

    @Override
    public void visit(Stmt node) {
        if (inIfStmt > 0) {
            answers.add(wrappedScratchBlocks(node));
        }
        else if (visitingScript) {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    @Override
    public String getName() {
        return "statements_in_if_statement";
    }
}
