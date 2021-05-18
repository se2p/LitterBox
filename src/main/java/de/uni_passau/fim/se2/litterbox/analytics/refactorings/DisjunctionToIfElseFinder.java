package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DisjunctionToIfElse;

public class DisjunctionToIfElseFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfThenStmt ifThenStmt) {

        if (ifThenStmt.getBoolExpr() instanceof Or) {
            refactorings.add(new DisjunctionToIfElse(ifThenStmt));
        }

        visitChildren(ifThenStmt);
    }
    @Override
    public String getName() {
        return DisjunctionToIfElse.NAME;
    }
}
