/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (statements.getNumberOfStatements() != 1 || !(statements.getStatement(0) instanceof RepeatForeverStmt repeatForeverStmt)) {
            return; // Only scripts consisting only of the loop
        }

        if (!repeatForeverStmt.getStmtList().hasStatements()) {
            return;
        }

        for (Stmt stmtForever : repeatForeverStmt.getStmtList().getStmts()) {
            // Check for ifThen statement.
            if (stmtForever instanceof IfThenStmt ifThenStmt) {
                // Check the bool expression for isKeyPressed event.
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
