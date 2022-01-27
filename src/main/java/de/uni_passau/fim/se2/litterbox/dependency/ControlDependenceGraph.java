/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.CloneEventNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.LinkedHashSet;
import java.util.Set;

public class ControlDependenceGraph extends AbstractDependencyGraph {

    public ControlDependenceGraph(ControlFlowGraph cfg) {
        super(cfg);
    }

    @Override
    protected MutableGraph<CFGNode> computeGraph() {

        PostDominatorTree pdt = new PostDominatorTree(cfg);
        MutableGraph<CFGNode> cdg = createUnconnectedGraph();

        // 1. Find S, a set of edges (U,V) in the CFG such that
        // V is not an ancestor of U in the post-dominator tree.
        Set<EndpointPair<CFGNode>> edges = new LinkedHashSet<>();
        for (EndpointPair<CFGNode> edge : cfg.getEdges()) {
            if (!pdt.isReachable(edge.target(), edge.source())) {
                edges.add(edge);
            }
        }

        for (EndpointPair<CFGNode> edge : edges) {
            // 2. For each edge (A,B) in S, find L, the least common ancestor
            // of A and B in the post-dominator tree.
            CFGNode lca = pdt.getLeastCommonAncestor(edge.source(), edge.target());

            // Traverse backwards in the post-dominator tree from B to L,
            // marking each node visited; mark L only if L = A.
            // Statements representing all marked nodes are control dependent on A.
            CFGNode current = edge.target();
            while (!current.equals(lca)) {
                cdg.putEdge(edge.source(), current);
                current = pdt.getPredecessors(current).iterator().next();
            }

            if (edge.source() == lca) {
                cdg.putEdge(edge.source(), lca);
            }
        }

        // Any nodes not connected at this point are only dependent on entry
        CFGNode entry = cfg.getEntryNode();
        for (CFGNode node : cdg.nodes()) {
            if (node != entry && cdg.inDegree(node) == 0) {
                if (node instanceof CloneEventNode) {
                    for (CFGNode pred : cfg.getPredecessors(node)) {
                        if (node.getASTNode() instanceof CreateCloneOf) {
                            cdg.putEdge(pred, node);
                        }
                    }

                }
                else {
                    cdg.putEdge(entry, node);
                }
            }
        }

        return cdg;
    }
}
