package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfElseToConjunction;

public class IfElseToConjunctionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        StmtList thenStmts = ifThenStmt.getThenStmts();
        if (thenStmts.getNumberOfStatements() == 1) {
            Stmt thenStmt = thenStmts.getStatement(0);
            if (thenStmt instanceof IfElseStmt) {
                refactorings.add(new IfElseToConjunction(ifThenStmt));
            }
        }

        visitChildren(ifThenStmt);
    }

    @Override
    public String getName() {
        return IfElseToConjunction.NAME;
    }
}
