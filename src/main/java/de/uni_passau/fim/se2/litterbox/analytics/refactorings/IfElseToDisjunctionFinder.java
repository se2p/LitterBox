package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfElseToDisjunction;

/*
If A:
  B
Else:
  If C:
    B

to

If A || C:
  B
 */
public class IfElseToDisjunctionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        StmtList elseStmts = ifElseStmt.getElseStmts();
        if (elseStmts.getNumberOfStatements() == 1) {
            Stmt elseStmt = elseStmts.getStatement(0);
            if (elseStmt instanceof IfThenStmt) {
                if (ifElseStmt.getThenStmts().equals(((IfThenStmt) elseStmt).getThenStmts())) {
                    refactorings.add(new IfElseToDisjunction(ifElseStmt, (IfThenStmt) elseStmt));
                }
            }
        }

        visitChildren(ifElseStmt);
    }

    @Override
    public String getName() {
        return IfElseToDisjunction.NAME;
    }
}
