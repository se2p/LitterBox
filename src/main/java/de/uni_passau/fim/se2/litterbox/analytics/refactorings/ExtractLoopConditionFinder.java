package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ExtractLoopCondition;

public class ExtractLoopConditionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {

        for (Stmt stmt : repeatForeverStmt.getStmtList().getStmts()) {
            if (stmt instanceof IfThenStmt) {
                IfThenStmt ifThenStmt = (IfThenStmt) stmt;
                if (ifThenStmt.getThenStmts().getNumberOfStatements() == 1 &&
                        ifThenStmt.getThenStmts().getStatement(0) instanceof TerminationStmt) {
                    refactorings.add(new ExtractLoopCondition(repeatForeverStmt, ifThenStmt));
                }
            }
        }

        visitChildren(repeatForeverStmt);
    }

    @Override
    public String getName() {
        return ExtractLoopCondition.NAME;
    }
}
