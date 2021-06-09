package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.InlineLoopCondition;

public class InlineLoopConditionFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Only refactor connected blocks
            return;
        }
        if (script.getStmtList().getNumberOfStatements() == 0) {
            return;
        }
        Stmt lastStmt = script.getStmtList().getStatement(script.getStmtList().getNumberOfStatements() - 1);
        if (lastStmt instanceof UntilStmt) {
            // TODO: Could also cover the case where it is followed by a TerminationStmt
            refactorings.add(new InlineLoopCondition((UntilStmt) lastStmt));
        }
    }

    @Override
    public String getName() {
        return InlineLoopCondition.NAME;
    }
}
