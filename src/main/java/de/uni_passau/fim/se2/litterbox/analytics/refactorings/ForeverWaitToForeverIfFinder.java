package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ForeverWaitToForeverIf;

public class ForeverWaitToForeverIfFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(RepeatForeverStmt loop) {
        if (loop.getStmtList().getNumberOfStatements() > 1) {
            Stmt initialStatement = loop.getStmtList().getStatement(0);
            if (initialStatement instanceof WaitUntil) {
                refactorings.add(new ForeverWaitToForeverIf(loop));
            }
        }
        visitChildren(loop);
    }

    @Override
    public String getName() {
        return ForeverWaitToForeverIf.NAME;
    }
}
