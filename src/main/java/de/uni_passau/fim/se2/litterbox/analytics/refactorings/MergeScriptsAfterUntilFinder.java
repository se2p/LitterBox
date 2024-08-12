/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
                if (script2Statements.isEmpty() || !(script2Statements.get(0) instanceof WaitUntil waitUntil)) {
                    continue;
                }

                if (script1Statements.isEmpty()
                        || !(script1Statements.get(script1Statements.size() - 1) instanceof UntilStmt untilStmt)) {
                    continue;
                }
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
