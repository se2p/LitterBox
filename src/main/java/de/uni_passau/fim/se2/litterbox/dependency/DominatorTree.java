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
package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.*;
import java.util.stream.Collectors;

public class DominatorTree extends AbstractDependencyGraph {

    public DominatorTree(ControlFlowGraph cfg) {
        super(cfg);
    }

    public CFGNode getLeastCommonAncestor(CFGNode node1, CFGNode node2) {
        CFGNode current = node1;
        Set<CFGNode> successors = getTransitiveSuccessors(current);

        while (!successors.contains(node1) || !successors.contains(node2)) {
            current = graph.predecessors(current).iterator().next();
            successors = getTransitiveSuccessors(current);
        }
        return current;
    }

    @Override
    protected MutableGraph<CFGNode> computeGraph() {

        MutableGraph<CFGNode> dominatorTree = createUnconnectedTree();
        Map<CFGNode, Set<CFGNode>> dominatorsOfNode = getStrictDominators(cfg);

        Queue<CFGNode> queue = new ArrayDeque<>();
        queue.add(cfg.getEntryNode());

        while (!queue.isEmpty()) {
            CFGNode currentNode = queue.poll();

            for (CFGNode otherNode : cfg.getNodes()) {
                Set<CFGNode> dominators = dominatorsOfNode.get(otherNode);
                if (dominators.contains(currentNode)) {
                    dominators.remove(currentNode);
                    if (dominators.isEmpty()) {
                        dominatorTree.putEdge(currentNode, otherNode);
                        queue.add(otherNode);
                    }
                }
            }
        }

        return dominatorTree;
    }

    private Map<CFGNode, Set<CFGNode>> getStrictDominators(ControlFlowGraph cfg) {
        Map<CFGNode, Set<CFGNode>> dominators = getDominators(cfg);
        for (CFGNode node : cfg.getNodes()) {
            dominators.get(node).remove(node);
        }

        return dominators;
    }

    private Map<CFGNode, Set<CFGNode>> getDominators(ControlFlowGraph cfg) {
        CFGNode entry = cfg.getEntryNode();
        Set<CFGNode> nodesWithoutEntry = cfg.getNodes().stream().filter(t -> t != entry).collect(Collectors.toSet());

        // D(entry) <- {entry}
        Map<CFGNode, Set<CFGNode>> dominators = new LinkedHashMap<>();
        dominators.put(entry, Sets.newHashSet(entry));
        // D(node \ {entry}) <- nodes
        for (CFGNode n : nodesWithoutEntry) {
            dominators.put(n, Sets.newHashSet(cfg.getNodes()));
        }

        boolean changed = true;
        while (changed) {
            changed = false;

            for (CFGNode node : nodesWithoutEntry) {
                Set<CFGNode> currentDominators = dominators.get(node);

                // newDominators = node \cup intersection(dominators for all predecessors)
                Set<CFGNode> newDominators = Sets.newHashSet(cfg.getNodes());
                for (CFGNode p : cfg.getPredecessors(node)) {
                    newDominators.retainAll(dominators.get(p));
                }
                newDominators.add(node);

                if (!newDominators.equals(currentDominators)) {
                    dominators.put(node, newDominators);
                    changed = true;
                }
            }
        }

        return dominators;
    }


}
