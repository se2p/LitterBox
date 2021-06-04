package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
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
    public void visit(Script script) {
        for (Stmt stmt : script.getStmtList().getStmts()) {
            // Check if there is a loop at the top level.
            if(stmt instanceof RepeatForeverStmt) {
                boolean isValid = false;
                RepeatForeverStmt repeatForeverStmt = (RepeatForeverStmt) stmt;
                for(Stmt stmtForever : repeatForeverStmt.getStmtList().getStmts()) {
                    // Check for ifThen statement.
                    if( stmtForever instanceof IfThenStmt) {
                        // Check the bool expression for isKeyPressed event.
                        IfThenStmt ifThenStmt = (IfThenStmt) stmtForever;
                        BoolExpr expr = ifThenStmt.getBoolExpr();
                        if(expr instanceof IsKeyPressed) {
                            isValid = true;
                        }
                    } else {
                        // If there are other stmts than ifThen
                        break;
                    }
                }
                if(isValid) {
                    refactorings.add(new ExtractEventsFromForever(script, repeatForeverStmt));
                }
            }
        }
    }

    @Override
    public String getName() {
        return ExtractEventsFromForever.NAME;
    }
}
