package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ConjunctionToIfs;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfsToConjunction;

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
