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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.SplitSlice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SplitSliceFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof Never) {
            // Unconnected blocks
            return;
        }

        ControlFlowGraph cfg = getControlFlowGraphForScript(script);
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);

        // Remove entry, exit, and event node
        pdg.removeNode(cfg.getEntryNode());
        pdg.removeNode(cfg.getExitNode());
        pdg.removeNode(script.getEvent());

        // Each component of the remaining graph is an independent set of statements
        Set<List<Stmt>> slices = new LinkedHashSet<>();
        Set<CFGNode> coveredNodes = new LinkedHashSet<>();

        for (CFGNode node : pdg.getNodes()) {
            if (coveredNodes.contains(node)) {
                continue;
            }
            Set<CFGNode> reachableNodes = pdg.getGraphComponentOf(node);
            coveredNodes.addAll(reachableNodes);

            // Some fuffing around necessary to ensure statements are in order
            Set<Stmt> sliceStmts = new LinkedHashSet<>();
            for (CFGNode reachableNode : reachableNodes) {
                if (reachableNode.getASTNode() instanceof Stmt) {
                    sliceStmts.add((Stmt) reachableNode.getASTNode());
                }
            }
            List<Stmt> slice = new ArrayList<>(script.getStmtList().getStmts());
            slice.retainAll(sliceStmts);
            slices.add(slice);
        }

        if (slices.size() > 1) {
            refactorings.add(new SplitSlice(script, slices));
        }
    }

    @Override
    public String getName() {
        return SplitSlice.NAME;
    }
}
