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
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SwapStatements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SwapStatementsFinder extends AbstractDependencyRefactoringFinder {

    private boolean inScript = true;

    private Script currentScript;

    private ProgramDependenceGraph pdg;

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            return;
        }
        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        pdg = new ProgramDependenceGraph(cfg);
        inScript = true;
        currentScript = script;
        visitChildren(script);
        inScript = false;
        currentScript = null;
    }

    @Override
    public void visit(StmtList stmtList) {
        if (!inScript) {
            return;
        }

        for (int i = 0; i < stmtList.getNumberOfStatements() - 1; i++) {
            Stmt stmt1 = stmtList.getStatement(i);
            Stmt stmt2 = stmtList.getStatement(i + 1);
            Optional<CFGNode> stmt1Node = pdg.getNode(stmt1);
            Optional<CFGNode> stmt2Node = pdg.getNode(stmt2);
            if (stmt1Node.isPresent() && stmt2Node.isPresent()
                    && !isTerminationStatement(stmt2Node.get().getASTNode())
                    && !pdg.hasDependency(stmt1Node.get(), stmt2Node.get())
                    && !wouldCreateDataDependency(currentScript, Arrays.asList(stmt2), Arrays.asList(stmt1))) {
                refactorings.add(new SwapStatements(stmt1, stmt2));
            }
        }
    }

    @Override
    protected boolean wouldCreateDataDependency(Script script, List<Stmt> subScript1, List<Stmt> subScript2) {
        List<Stmt> mergedStatements = new ArrayList<>(subScript1);
        mergedStatements.addAll(subScript2);
        Script mergedScript = new Script(script.getEvent(), new StmtList(mergedStatements));
        ControlFlowGraph cfg = getControlFlowGraphForScript(mergedScript);
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        return pdg.hasDependencyEdge(subScript1, subScript2);
    }

    @Override
    public String getName() {
        return SwapStatements.NAME;
    }
}
