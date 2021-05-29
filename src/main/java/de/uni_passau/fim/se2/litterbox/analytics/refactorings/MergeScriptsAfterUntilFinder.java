package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeScriptsAfterUntil;

import java.util.List;

public class MergeScriptsAfterUntilFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {

        for (Script script1 : scriptList.getScriptList()) {
            for (Script script2 : scriptList.getScriptList()) {
                if (script1 == script2) {
                    continue;
                }

                if (!script1.getEvent().equals(script2.getEvent()) || script1.getEvent() instanceof Never) {
                    continue;
                }

                List<Stmt> script1Statements = script1.getStmtList().getStmts();
                List<Stmt> script2Statements = script2.getStmtList().getStmts();
                if (script2Statements.isEmpty() || !(script2Statements.get(0) instanceof WaitUntil)) {
                    continue;
                }
                WaitUntil waitUntil = (WaitUntil) script2Statements.get(0);

                if (script1Statements.isEmpty() || !(script1Statements.get(script1Statements.size() - 1) instanceof UntilStmt)) {
                    continue;
                }
                UntilStmt untilStmt = (UntilStmt) script1Statements.get(script1Statements.size() - 1);
                if (untilStmt.getBoolExpr().equals(waitUntil.getUntil())) {
                    refactorings.add(new MergeScriptsAfterUntil(script1, script2, untilStmt));
                }
            }
        }
    }

    @Override
    public String getName() {
        return MergeScriptsAfterUntil.NAME;
    }
}
