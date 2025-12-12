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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitInitializationScript;

import java.util.ArrayList;
import java.util.List;

public class SplitInitializationScriptFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return;
        }

        StmtList stmts = script.getStmtList();
        List<Stmt> stmtList = stmts.getStmts();
        if (stmtList.isEmpty()) {
            return;
        }

        int splitIndex = -1;
        for (int i = 0; i < stmtList.size(); i++) {
            Stmt stmt = stmtList.get(i);
            if (isTimeConsuming(stmt)) {
                splitIndex = i;
                break;
            } else if (!isAttributeSetting(stmt)) {
                // If we encounter a statement that is neither attribute setting nor time consuming,
                // then the prefix is broken (or at least it's not a pure initialization prefix).
                // We stop looking.
                return;
            }
        }

        // If we found a split point and it's not the first statement (meaning there is some initialization)
        if (splitIndex > 0) {
            Stmt splitPoint = stmtList.get(splitIndex);

            // Check dependencies
            ControlFlowGraph cfg = getControlFlowGraphForScript(script);
            ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
            TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
            DataDependenceGraph ddg = new DataDependenceGraph(cfg);

            List<Stmt> stmts1 = new ArrayList<>(stmtList.subList(0, splitIndex));
            List<Stmt> stmts2 = new ArrayList<>(stmtList.subList(splitIndex, stmtList.size()));

            if (!cdg.hasDependencyEdge(stmts1, stmts2)
                    && !tdg.hasDependencyEdge(stmts1, stmts2)
                    && !ddg.hasDependencyEdge(stmts1, stmts2)
                    && !wouldCreateDataDependency(script, stmts2, stmts1)) {
                refactorings.add(new SplitInitializationScript(script, splitPoint));
            }
        }
    }

    private boolean isTimeConsuming(Stmt stmt) {
        return stmt instanceof TimedStmt
                || stmt instanceof WaitUntil
                || stmt instanceof AskAndWait
                || stmt instanceof LoopStmt;
    }

    @Override
    public String getName() {
        return SplitInitializationScript.NAME;
    }
}
