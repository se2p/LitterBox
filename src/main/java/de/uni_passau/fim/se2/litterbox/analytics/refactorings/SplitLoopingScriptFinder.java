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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitLoopingScript;

import java.util.Collections;
import java.util.List;

public class SplitLoopingScriptFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return;
        }

        if (script.getStmtList().getStmts().size() != 1) {
            return;
        }

        Stmt stmt = script.getStmtList().getStmts().getFirst();
        if (!(stmt instanceof LoopStmt)) {
            return;
        }

        StmtList loopBody;
        switch (stmt) {
            case RepeatForeverStmt forever -> loopBody = forever.getStmtList();
            case RepeatTimesStmt repeat -> loopBody = repeat.getStmtList();
            case UntilStmt until -> loopBody = until.getStmtList();
            default -> {
                return;
            }
        }

        List<Stmt> bodyStmts = loopBody.getStmts();
        if (bodyStmts.size() <= 1) {
            return;
        }

        for (Stmt s : bodyStmts) {
            if (!(s instanceof IfStmt)) {
                return;
            }
        }

        // Check dependencies between if-statements
        // They must be independent of each other (no order dependency)

        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        for (int i = 0; i < bodyStmts.size(); i++) {
            for (int j = 0; j < bodyStmts.size(); j++) {
                if (i == j) {
                    continue;
                }

                List<Stmt> stmt1 = Collections.singletonList(bodyStmts.get(i));
                List<Stmt> stmt2 = Collections.singletonList(bodyStmts.get(j));

                if (cdg.hasDependencyEdge(stmt1, stmt2)
                        || tdg.hasDependencyEdge(stmt1, stmt2)
                        || ddg.hasDependencyEdge(stmt1, stmt2)) {
                    return;
                }
            }
        }

        refactorings.add(new SplitLoopingScript(script));
    }

    @Override
    public String getName() {
        return SplitLoopingScript.NAME;
    }
}
