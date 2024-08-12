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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeLoops;

import java.util.ArrayList;
import java.util.List;

public class MergeLoopsFinder extends AbstractDependencyRefactoringFinder {

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

                StmtList stmtList1 = script1.getStmtList();
                StmtList stmtList2 = script2.getStmtList();

                if (stmtList1.getNumberOfStatements() != 1 || stmtList2.getNumberOfStatements() != 1) {
                    continue;
                }

                if (!stmtList1.getStatement(0).getClass().equals(stmtList2.getStatement(0).getClass())
                        || !(stmtList1.getStatement(0) instanceof LoopStmt)) {
                    continue;
                }

                if (!hasDependencies(script1, script2)) {
                    refactorings.add(new MergeLoops(script1, script2));
                }
            }
        }
    }

    /*
     * Since the dependency analysis does not take concurrency into account
     * this method simply applies the merge and checks for dependencies in
     * the resulting script that span across the two parent scripts.
     */
    private boolean hasDependencies(Script script1, Script script2) {

        MergeLoops refactoring = new MergeLoops(script1, script2);
        Script merged = refactoring.getMergedScript();
        StmtList mergedStatements = ((LoopStmt) merged.getStmtList().getStatement(0)).getStmtList();

        LoopStmt loop1 = (LoopStmt) script1.getStmtList().getStatement(0);
        LoopStmt loop2 = (LoopStmt) script2.getStmtList().getStatement(0);

        if (!loop1.getStmtList().hasStatements() || !loop2.getStmtList().hasStatements()) {
            // If one of the loops is empty, merging is always possible
            return false;
        }

        ControlFlowGraph cfg = getControlFlowGraphForScript(merged);

        List<Stmt> stmtScript1 = new ArrayList<>(mergedStatements.getStmts().subList(0, loop1.getStmtList().getNumberOfStatements()));
        List<Stmt> stmtScript2 = new ArrayList<>(mergedStatements.getStmts().subList(loop1.getStmtList().getNumberOfStatements(), mergedStatements.getNumberOfStatements()));

        if (hasControlDependency(cfg, stmtScript1, stmtScript2)
                || hasDataDependency(cfg, stmtScript1, stmtScript2)
                || hasTimeDependency(cfg, stmtScript1, stmtScript2)
                || wouldCreateDataDependency(merged, stmtScript2, stmtScript1)) {
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return MergeLoops.NAME;
    }
}
