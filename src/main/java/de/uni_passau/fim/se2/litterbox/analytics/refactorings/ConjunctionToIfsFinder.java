package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ConjunctionToIfs;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfsToConjunction;

/*
If A && B:
  C

to

If A:
  If B:
    C
 */
public class ConjunctionToIfsFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {

        if (ifThenStmt.getBoolExpr() instanceof And) {
            refactorings.add(new ConjunctionToIfs(ifThenStmt));
        }

        visitChildren(ifThenStmt);
    }

    @Override
    public String getName() {
        return IfsToConjunction.NAME;
    }
}
