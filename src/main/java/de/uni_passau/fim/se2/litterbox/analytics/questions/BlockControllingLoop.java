package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices {@code MAX_CHOICES}
 * @Highlighted Loop
 * @Context Single script
 */
public class BlockControllingLoop extends AbstractQuestionFinder {

    private boolean loopFound;
    private IssueBuilder builder;

    public void visit(Script node) {
        loopFound = false;
        builder = null;
        choices.clear();
        answers.clear();
        super.visit(node);

        if (loopFound && choices.size() > 0) {
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.CHOICES, getChoices());
            hint.setParameter(Hint.ANSWER, getAnswers());
            addIssue(builder.withHint(hint));
        }
    }

    public void visit(RepeatTimesStmt node) {
        NumExpr times = node.getTimes();

        if (!loopFound) {
            loopFound = true;
            answers.add((times instanceof NumberLiteral numberLiteral) ?
                    wrappedScratchBlocks(numberLiteral) : wrappedScratchBlocks(times));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            visit(times);
        }
        visit(node.getStmtList());
    }

    public void visit(UntilStmt node) {
        if (!loopFound) {
            loopFound = true;
            answers.add(wrappedScratchBlocks(node.getBoolExpr()));
            builder = prepareIssueBuilder(node).withSeverity(IssueSeverity.LOW);
        } else {
            choices.add(wrappedScratchBlocks(node.getBoolExpr()));
        }
        visit(node.getStmtList());
    }

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
        super.visit(node);
    }

    public void visit(BoolExpr node) {
        choices.add(wrappedScratchBlocks(node));
        super.visit(node);
    }

    public void visit(NumExpr node) {
        if (node instanceof NumberLiteral numberLiteral) {
            choices.add(wrappedScratchBlocks(numberLiteral));
        }
        else {
            choices.add(wrappedScratchBlocks(node));
        }
    }

    public void visit(TerminationStmt node) {
        // Don't include Termination statements as options
    }

    @Override
    public String getName() {
        return "block_controlling_loop";
    }
}
