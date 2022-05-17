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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitLoop;

import java.util.ArrayList;
import java.util.List;

/*
repeat:
  A
  B

to

repeat:
  A
repeat:
  B
 */
public class SplitLoopFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {

        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        if (script.getStmtList().getNumberOfStatements() != 1) {
            return;
        }

        if (!(script.getStmtList().getStatement(0) instanceof LoopStmt)) {
            return;
        }

        LoopStmt loop = (LoopStmt) script.getStmtList().getStatement(0);

        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        // If the loop execution depends on its content we can never split
        if (cdg.hasDependencyEdge(loop.getStmtList().getStmts(), loop)) {
            return;
        }

        StmtList stmts = loop.getStmtList();
        for (int i = 1; i < stmts.getStmts().size(); i++) {
            Stmt splitPoint = stmts.getStmts().get(i);

            List<Stmt> stmts1 = new ArrayList<>(stmts.getStmts().subList(0, i));
            List<Stmt> stmts2 = new ArrayList<>(stmts.getStmts().subList(i, stmts.getNumberOfStatements()));

            if (!cdg.hasDependencyEdge(stmts1, stmts2)
                    && !tdg.hasDependencyEdge(stmts1, stmts2)
                    && !ddg.hasDependencyEdge(stmts1, stmts2)
                    && !wouldCreateDataDependency(script, stmts2, stmts1)) {
                refactorings.add(new SplitLoop(script, loop, splitPoint));
            }
        }
    }


    @Override
    public String getName() {
        return SplitLoop.NAME;
    }
}
