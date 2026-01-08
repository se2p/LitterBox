/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeInitializationScript;

public class MergeInitializationScriptFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {
        for (Script script1 : scriptList.getScriptList()) {
            if (!isInitializationScript(script1)) {
                continue;
            }

            for (Script script2 : scriptList.getScriptList()) {
                if (script1 == script2) {
                    continue;
                }

                if (!isLoopScript(script2)) {
                    continue;
                }

                if (!script1.getEvent().equals(script2.getEvent()) || !(script1.getEvent() instanceof GreenFlag)) {
                    continue;
                }

                if (!hasDependencies(script1, script2)) {
                    refactorings.add(new MergeInitializationScript(script1, script2));
                }
            }
        }
    }

    private boolean isInitializationScript(Script script) {
        if (!(script.getEvent() instanceof GreenFlag)) {
            return false;
        }
        if (script.getStmtList().getStmts().isEmpty()) {
            return false;
        }
        for (Stmt stmt : script.getStmtList().getStmts()) {
            if (!isAttributeSetting(stmt)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLoopScript(Script script) {
        if (!(script.getEvent() instanceof GreenFlag)) {
            return false;
        }
        if (script.getStmtList().getStmts().size() != 1) {
            return false;
        }
        return script.getStmtList().getStmts().getFirst() instanceof LoopStmt;
    }

    /*
     * Since the dependency analysis does not take concurrency into account
     * this method simply applies the merge and checks for dependencies in
     * the resulting script that span across the two parent scripts.
     */
    protected boolean hasDependencies(Script script1, Script script2) {
        MergeInitializationScript refactoring = new MergeInitializationScript(script1, script2);
        Script merged = refactoring.getMergedScript();
        return super.hasDependencies(script1, merged);
    }

    @Override
    public String getName() {
        return MergeInitializationScript.NAME;
    }
}
