package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitScriptAfterUntil;

public class SplitScriptAfterUntilFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(Script script) {

        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        for (int i = 0; i < script.getStmtList().getNumberOfStatements() - 1; i++) {
            Stmt stmt = script.getStmtList().getStatement(i);
            if (stmt instanceof UntilStmt) {
                refactorings.add(new SplitScriptAfterUntil(script, (UntilStmt) stmt));
            }
        }
    }

    @Override
    public String getName() {
        return SplitScriptAfterUntil.NAME;
    }
}
