package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.IfElseToIfIfNot;

public class IfElseToIfIfNotFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        refactorings.add(new IfElseToIfIfNot(ifElseStmt));
        visitChildren(ifElseStmt);
    }

    @Override
    public String getName() {
        return IfElseToIfIfNot.NAME;
    }
}
