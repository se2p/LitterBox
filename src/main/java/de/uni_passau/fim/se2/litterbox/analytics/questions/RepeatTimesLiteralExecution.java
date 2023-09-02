package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

public class RepeatTimesLiteralExecution extends AbstractIssueFinder {

    private Boolean hasStop;

    @Override
    public void visit(RepeatTimesStmt node) {
        hasStop = false;

        if (node.getTimes() instanceof NumberLiteral num) {
            super.visit(node);

            if (node.getStmtList().hasStatements() && !hasStop){
                ASTNode stmt = node.getStmtList().getStmts().get(0);

                IssueBuilder builder = prepareIssueBuilder(stmt).withSeverity(IssueSeverity.LOW);
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.BLOCK_NAME, stmt.getScratchBlocks());
                hint.setParameter(Hint.ANSWER, String.valueOf(num.getValue()));
                addIssue(builder.withHint(hint));
            }
        }
    }

    @Override
    public void visit(TerminationStmt node) {
        hasStop = true;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.QUESTION;
    }

    @Override
    public String getName() {
        return "repeat_times_literal_execution";
    }
}
