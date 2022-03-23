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
package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramDependenceGraph extends AbstractDependencyGraph {

    public ProgramDependenceGraph(ControlFlowGraph cfg) {
        super(cfg);
    }

    public boolean hasAnyDependencies(CFGNode node) {
        return graph.inDegree(node) > 1; // Always dependent on entry because that's in the CDG
    }

    public boolean hasDependency(CFGNode node1, CFGNode node2) {
        return graph.hasEdgeConnecting(node1, node2);
    }

    @Override
    protected MutableGraph<CFGNode> computeGraph() {

        DataDependenceGraph    ddg = new DataDependenceGraph(cfg);
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        TimeDependenceGraph    tdg = new TimeDependenceGraph(cfg);
        MutableGraph<CFGNode>  pdg = createUnconnectedGraph();

        for (CFGNode node : cfg.getNodes()) {
            cdg.getSuccessors(node).forEach(succ -> pdg.putEdge(node, succ));
            ddg.getSuccessors(node).forEach(succ -> pdg.putEdge(node, succ));
            tdg.getSuccessors(node).forEach(succ -> pdg.putEdge(node, succ));
        }

        return pdg;
    }

    public Set<Stmt> backwardSlice(Stmt target) {
        return backwardSlice(Arrays.asList(target));
    }

    public Set<Stmt> forwardSlice(Stmt target) {
        return forwardSlice(Arrays.asList(target));
    }

    public Set<Stmt> backwardSlice(Collection<Stmt> target) {
        Set<CFGNode> targetNodes = target.stream().map(s -> getNode(s).get()).collect(Collectors.toSet());
        Set<CFGNode> slice = backwardSliceNodes(targetNodes);
        return slice.stream().filter(n -> n.getASTNode() instanceof Stmt).map(n -> (Stmt) n.getASTNode()).collect(Collectors.toSet());
    }

    public Set<Stmt> forwardSlice(Collection<Stmt> target) {
        Set<CFGNode> targetNodes = target.stream().map(s -> getNode(s).get()).collect(Collectors.toSet());
        Set<CFGNode> slice = forwardSliceNodes(targetNodes);
        return slice.stream().filter(n -> n.getASTNode() instanceof Stmt).map(n -> (Stmt) n.getASTNode()).collect(Collectors.toSet());
    }

    public Set<CFGNode> backwardSliceNodes(Collection<CFGNode> target) {
        Set<CFGNode> slice = new LinkedHashSet<>();
        Queue<CFGNode> queue = new ArrayDeque<>();
        queue.addAll(target);

        while (!queue.isEmpty()) {
            CFGNode node = queue.poll();
            if (!slice.contains(node)) {
                slice.add(node);
                queue.addAll(graph.predecessors(node));
            }
        }

        return slice;
    }

    public Set<CFGNode> forwardSliceNodes(Collection<CFGNode> source) {
        Set<CFGNode> slice = new LinkedHashSet<>();
        Queue<CFGNode> queue = new ArrayDeque<>();
        queue.addAll(source);

        while (!queue.isEmpty()) {
            CFGNode node = queue.poll();
            if (!slice.contains(node)) {
                slice.add(node);
                queue.addAll(graph.successors(node));
            }
        }

        return slice;
    }

}
