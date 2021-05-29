package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

public class MergeEventHandlerFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        for (Stmt stmt : repeatForeverStmt.getStmtList().getStmts()) {
            if(stmt instanceof IfThenStmt) {
                IfThenStmt ifThenStmt = (IfThenStmt) stmt;
                if(ifThenStmt.getThenStmts().getNumberOfStatements() == 1) {

                }
                System.out.println("Test");
            }
        }
    }


    @Override
    public String getName() {
        return MergeEventHandler.NAME;
    }
}
