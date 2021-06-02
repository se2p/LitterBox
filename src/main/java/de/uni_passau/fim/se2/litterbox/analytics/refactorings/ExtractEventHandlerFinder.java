package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ExtractEventHandler;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

public class ExtractEventHandlerFinder extends AbstractRefactoringFinder {


    @Override
    public void visit(RepeatForeverStmt repeatForeverStmt) {
        for (Stmt stmt : repeatForeverStmt.getStmtList().getStmts()) {
            if(stmt instanceof IfThenStmt) {
                IfThenStmt ifThenStmt = (IfThenStmt) stmt;
                // Check if bool expression is an isKeyPressed Event
                BoolExpr expr = ifThenStmt.getBoolExpr();
                if(expr instanceof IsKeyPressed || expr instanceof IsMouseDown)
                    refactorings.add(new ExtractEventHandler(repeatForeverStmt, ifThenStmt));
            }
        }
        visitChildren(repeatForeverStmt);
    }

    @Override
    public String getName() {
        return ExtractEventHandler.NAME;
    }
}
