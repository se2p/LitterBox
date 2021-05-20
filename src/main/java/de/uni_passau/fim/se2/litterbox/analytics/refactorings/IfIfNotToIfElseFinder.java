package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfIfNotToIfElse;

import java.util.List;

/*
If A:
  B
if not A
  C

to

If A:
  B
Else:
  C:
 */
public class IfIfNotToIfElseFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(StmtList node) {
        final List<Stmt> stmts = node.getStmts();

        for (int i = 0; i < stmts.size() - 1; i++) {
            if (stmts.get(i) instanceof IfThenStmt &&
                    stmts.get(i + 1) instanceof IfThenStmt) {
                IfThenStmt ifThenStmt1 = (IfThenStmt) stmts.get(i);
                IfThenStmt ifThenStmt2 = (IfThenStmt) stmts.get(i + 1);

                // TODO: Negation could also be the other way round?
                if (ifThenStmt2.getBoolExpr() instanceof Not) {
                    Not negation = (Not) ifThenStmt2.getBoolExpr();
                    if (negation.getOperand1().equals(ifThenStmt1.getBoolExpr())) {
                        refactorings.add(new IfIfNotToIfElse(ifThenStmt1, ifThenStmt2));
                    }
                }
            }
        }

        visitChildren(node);
    }

    @Override
    public String getName() {
        return IfIfNotToIfElse.NAME;
    }
}
