package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitIf;

public class SplitIfFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        StmtList thenStmts = ifThenStmt.getThenStmts();
        if (thenStmts.getNumberOfStatements() > 1) {
            refactorings.add(new SplitIf(ifThenStmt));
        }

        visitChildren(ifThenStmt);
    }

    @Override
    public String getName() {
        return SplitIf.NAME;
    }
}
