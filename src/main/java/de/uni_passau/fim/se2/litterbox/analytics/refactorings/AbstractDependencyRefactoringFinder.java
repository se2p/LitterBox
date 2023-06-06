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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.CloneEventNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ControlDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.TimeDependenceGraph;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDependencyRefactoringFinder extends AbstractRefactoringFinder {

    protected boolean endsWithTerminationStatement(StmtList stmtList) {
        int numStatements1 = stmtList.getNumberOfStatements();
        if (numStatements1 > 0) {
            Stmt lastStmt = stmtList.getStatement(numStatements1 - 1);
            if (isTerminationStatement(lastStmt)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTerminationStatement(ASTNode node) {
        return node instanceof RepeatForeverStmt || node instanceof TerminationStmt;
    }

    protected ControlFlowGraph getControlFlowGraphForScript(Script script) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        script.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();

        // "When I start as clone" usually has no edge from entry
        // as it is called by an interprocedural edge. Since this
        // is not an interprocedural CFG, we need to add the missing edge
        // TODO: Create a builder for intraprocedural CFGs
        if (script.getEvent() instanceof StartedAsClone) {
            CFGNode head = cfg.getNodes().stream().filter(CloneEventNode.class::isInstance).findFirst().get();
            cfg.addEdgeFromEntry(head);
        } else if (script.getEvent() instanceof ReceptionOfMessage reception) {
            CFGNode head = cfg.getNode(reception.getMsg()).get();
            cfg.addEdgeFromEntry(head);
        }
        return cfg;
    }

    protected boolean hasControlDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        // If subScript2 has a control dependency on subScript1 then false, else true
        return cdg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean hasDataDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        // If subScript2 has a data dependency on subScript1 then false
        return ddg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean hasTimeDependency(ControlFlowGraph cfg, List<Stmt> subScript1, List<Stmt> subScript2) {
        TimeDependenceGraph tdg = new TimeDependenceGraph(cfg);
        // If subScript2 has a time dependency on subScript1 then false, else true
        return tdg.hasDependencyEdge(subScript1, subScript2);
    }

    protected boolean wouldCreateDataDependency(Script script, List<Stmt> subScript1, List<Stmt> subScript2) {
        List<Stmt> mergedStatements = new ArrayList<>(subScript1);
        mergedStatements.addAll(subScript2);
        Script mergedScript = new Script(script.getEvent(), new StmtList(mergedStatements));
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(currentActor);
        mergedScript.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);
        return ddg.hasDependencyEdge(subScript1, subScript2);
    }
}
