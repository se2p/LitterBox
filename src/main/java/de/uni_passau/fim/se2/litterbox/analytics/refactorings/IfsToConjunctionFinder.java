package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfsToConjunction;

/*
If A:
  If B:
    C

to

If A && B:
  C
 */
public class IfsToConjunctionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {
        StmtList thenStmts = ifThenStmt.getThenStmts();
        if (thenStmts.getNumberOfStatements() == 1) {
            Stmt thenStmt = thenStmts.getStatement(0);
            if (thenStmt instanceof IfThenStmt) {
                IfThenStmt ifThenStmt2 = (IfThenStmt) thenStmt;
                if (!ifThenStmt.getBoolExpr().equals(ifThenStmt2.getBoolExpr())) {
                    refactorings.add(new IfsToConjunction(ifThenStmt, (IfThenStmt) thenStmt));
                }
            }
        }

        visitChildren(ifThenStmt);
    }

    @Override
    public String getName() {
        return IfsToConjunction.NAME;
    }
}
