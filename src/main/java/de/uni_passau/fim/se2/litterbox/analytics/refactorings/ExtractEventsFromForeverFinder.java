package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ExtractEventsFromForever;


/*
    forever
        if isKeyPressed than
            A
        if isKeyPressed than
            B

     to

     KeyPressed
        A

     KeyPressed
        B
 */

public class ExtractEventsFromForeverFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {
        for (Script script : scriptList.getScriptList()) {
            checkScript(scriptList, script);
        }
    }

    private void checkScript(ScriptList scriptList, Script script) {
        if (!(script.getEvent() instanceof GreenFlag)) {
            return;
        }

        StmtList statements = script.getStmtList();
        if (statements.getNumberOfStatements() != 1 || !(statements.getStatement(0) instanceof RepeatForeverStmt)) {
            return; // Only scripts consisting only of the loop
        }

        RepeatForeverStmt repeatForeverStmt = (RepeatForeverStmt) statements.getStatement(0);

        for(Stmt stmtForever : repeatForeverStmt.getStmtList().getStmts()) {
            // Check for ifThen statement.
            if (stmtForever instanceof IfThenStmt) {
                // Check the bool expression for isKeyPressed event.
                IfThenStmt ifThenStmt = (IfThenStmt) stmtForever;
                BoolExpr expr = ifThenStmt.getBoolExpr();
                if (!(expr instanceof IsKeyPressed)) {
                    return;
                }
            } else {
                return;
            }
        }

        refactorings.add(new ExtractEventsFromForever(scriptList, script, repeatForeverStmt));
    }

    @Override
    public String getName() {
        return ExtractEventsFromForever.NAME;
    }
}
