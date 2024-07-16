package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

/**
 * @QuestionType Number
 * @Highlighted Statement
 * @Context Single script
 */
public class RepeatTimesLiteralExecution extends AbstractQuestionFinder {

    private boolean hasStop;
    private boolean inLoop;

    @Override
    public void visit(LoopStmt node) {
        inLoop = true;
        super.visit(node);
        inLoop = false;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (!inLoop) {
            inLoop = true;
            hasStop = false;

            if (node.getTimes() instanceof NumberLiteral num) {
                super.visit(node);

                if (node.getStmtList().hasStatements() && !hasStop) {
                    ASTNode stmt = getSingleStmt(node.getStmtList().getStmts().get(0));

                    IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                    Hint hint = new Hint(getName());
                    hint.setParameter(Hint.STATEMENT, stmt.getScratchBlocksWithoutNewline());
                    hint.setParameter(Hint.ANSWER, String.valueOf(num.getValue()));
                    addIssue(builder.withHint(hint));
                }
            }
        }
        else {
            visit(node.getStmtList());
        }
    }

    @Override
    public void visit(TerminationStmt node) {
        hasStop = true;
    }

    @Override
    public String getName() {
        return "repeat_times_literal_execution";
    }
}
