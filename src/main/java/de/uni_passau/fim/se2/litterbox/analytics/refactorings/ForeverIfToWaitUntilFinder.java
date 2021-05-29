package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ForeverIfToWaitUntil;

public class ForeverIfToWaitUntilFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt loop) {
        if (loop.getStmtList().getNumberOfStatements() == 1) {
            Stmt loopBody = loop.getStmtList().getStatement(0);
            if (loopBody instanceof IfThenStmt) {
                IfThenStmt ifThenStmt = (IfThenStmt) loopBody;
                refactorings.add(new ForeverIfToWaitUntil(loop));
            }
        }
        visitChildren(loop);
    }

    @Override
    public String getName() {
        return ForeverIfToWaitUntil.NAME;
    }
}
