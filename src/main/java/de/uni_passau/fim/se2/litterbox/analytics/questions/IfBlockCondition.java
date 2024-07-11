package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted If Statement
 * @Context Single script
 */
public class IfBlockCondition extends AbstractQuestionFinder {
    private boolean ifStmtFound;
    private IssueBuilder builder;

    @Override
    public void visit(Script node) {
        ifStmtFound = false;
        builder = null;
        choices.clear();
        answers.clear();
        super.visit(node);

        if (ifStmtFound && !choices.isEmpty()) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, answers.toString());
            addIssue(builder.withHint(hint));
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!ifStmtFound) {
            ifStmtFound = true;
            answers.add(wrappedScratchBlocks(node.getBoolExpr()));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            visit(node.getBoolExpr());
        }

        visit(node.getThenStmts());
    }

    @Override
    public void visit(IfElseStmt node) {
        if (!ifStmtFound) {
            ifStmtFound = true;
            answers.add(wrappedScratchBlocks(node.getBoolExpr()));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            visit(node.getBoolExpr());
        }

        visit(node.getThenStmts());
        visit(node.getElseStmts());
    }

    @Override
    public void visit(ControlStmt node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof BoolExpr boolExpr) {
                visit(boolExpr);
            }
            else if (child instanceof Stmt stmt) {
                visit(stmt);
            }
            else {
                visit(child);
            }
        }
    }

    public void visit(Stmt node) {
        choices.add(wrappedScratchBlocks(node));
    }

    public void visit(BoolExpr node) {
        choices.add(wrappedScratchBlocks(node));
    }

    @Override
    public String getName() {
        return "if_block_condition";
    }
}
