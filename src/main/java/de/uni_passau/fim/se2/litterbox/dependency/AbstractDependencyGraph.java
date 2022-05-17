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

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.CloneEventNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDependencyGraph {

    protected MutableGraph<CFGNode> graph;

    protected ControlFlowGraph cfg;

    protected AbstractDependencyGraph(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.graph = computeGraph();
    }

    protected abstract MutableGraph<CFGNode> computeGraph();

    protected MutableGraph<CFGNode> createUnconnectedGraph() {
        MutableGraph<CFGNode> graph = GraphBuilder.directed().allowsSelfLoops(true).build();
        fillGraphWithCFGNodes(graph);
        return graph;
    }

    protected MutableGraph<CFGNode> createUnconnectedTree() {
        MutableGraph<CFGNode> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        fillGraphWithCFGNodes(graph);
        return graph;
    }

    private void fillGraphWithCFGNodes(MutableGraph<CFGNode> graph) {
        cfg.getNodes().forEach(graph::addNode);
    }

    public Set<CFGNode> getPredecessors(CFGNode node) {
        return graph.predecessors(node);
    }

    public Set<CFGNode> getSuccessors(CFGNode node) {
        return graph.successors(node);
    }

    public Set<CFGNode> getTransitiveSuccessors(CFGNode node) {
        return Graphs.reachableNodes(graph, node);
    }

    public Set<CFGNode> getGraphComponentOf(CFGNode node) {
        Set<CFGNode> nodes = new LinkedHashSet<>();
        Queue<CFGNode> toVisit = new ArrayDeque<>();
        toVisit.add(node);

        while (!toVisit.isEmpty()) {
            CFGNode current = toVisit.remove();
            if (!nodes.add(current)) {
                continue;
            }

            for (CFGNode neighbour : graph.adjacentNodes(current)) {
                if (!nodes.contains(neighbour)) {
                    toVisit.add(neighbour);
                }
            }
        }

        return nodes;
    }

    public Set<CFGNode> getNodes() {
        return Collections.unmodifiableSet(graph.nodes());
    }

    public ControlFlowGraph getCFG() {
        return cfg;
    }

    public Set<EndpointPair<CFGNode>> getEdges() {
        return Collections.unmodifiableSet(graph.edges());
    }

    public int getNumNodes() {
        return graph.nodes().size();
    }

    public int getNumEdges() {
        return graph.edges().size();
    }

    public void removeNode(CFGNode node) {
        graph.removeNode(node);
    }

    public void removeNode(ASTNode node) {
        if (node instanceof ReceptionOfMessage) {
            // Rely on exception if this doesn't exist
            CFGNode cfgNode = getNode(((ReceptionOfMessage) node).getMsg()).get();
            graph.removeNode(cfgNode);
        } else if (node instanceof StartedAsClone) {
            // Rely on exception if this doesn't exist
            CFGNode cfgNode = cfg.stream().filter(CloneEventNode.class::isInstance).findFirst().get();
            graph.removeNode(cfgNode);
        } else {
            CFGNode cfgNode = getNode(node).get();
            graph.removeNode(cfgNode);
        }
    }

    public Optional<CFGNode> getNode(ASTNode node) {
        return graph.nodes().stream().filter(n -> n.getASTNode() == node).findFirst();
    }

    public Set<CFGNode> getCFGNodes(Collection<Stmt> astNodes) {
        Set<Stmt> transitiveStatements = new LinkedHashSet<>();
        astNodes.forEach(s -> transitiveStatements.addAll(getTransitiveStatements(s)));
        return graph.nodes().stream().filter(n -> transitiveStatements.contains(n.getASTNode())).collect(Collectors.toSet());
    }

    public boolean hasDependencyEdge(Collection<Stmt> sourceNodes, Stmt targetNode) {
        Set<CFGNode> sourceCFGNodes = getCFGNodes(sourceNodes);
        CFGNode targetCFGNode = getNode(targetNode).get();

        for (EndpointPair<CFGNode> edge : graph.edges()) {
            if (sourceCFGNodes.contains(edge.nodeU()) && targetCFGNode == edge.nodeV()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasDependencyEdge(Collection<Stmt> sourceNodes, Collection<Stmt> targetNodes) {
        Set<CFGNode> sourceCFGNodes = getCFGNodes(sourceNodes);
        Set<CFGNode> targetCFGNodes = getCFGNodes(targetNodes);

        for (EndpointPair<CFGNode> edge : graph.edges()) {
            if (sourceCFGNodes.contains(edge.nodeU()) && targetCFGNodes.contains(edge.nodeV())) {
                return true;
            }
            if (sourceCFGNodes.contains(edge.nodeV()) && targetCFGNodes.contains(edge.nodeU())) {
                return true;
            }
        }

        return false;
    }

    private Set<Stmt> getTransitiveStatements(Stmt stmt) {
        return getTransitiveNodes(stmt).stream().filter(Stmt.class::isInstance).map(Stmt.class::cast).collect(Collectors.toSet());
    }

    private Set<ASTNode> getTransitiveNodes(ASTNode node) {
        Set<ASTNode> nodes = new LinkedHashSet<>();
        nodes.add(node);
        nodes.addAll(node.getChildren());
        for (ASTNode child : node.getChildren()) {
            nodes.addAll(getTransitiveNodes(child));
        }
        return nodes;
    }

    public boolean isReachable(CFGNode source, CFGNode target) {
        return Graphs.reachableNodes(graph, source).contains(target);
    }

    public String toDotString() {
        StringBuilder builder = new StringBuilder();

        builder.append("digraph {");
        builder.append(System.lineSeparator());

        for (EndpointPair<CFGNode> edge : graph.edges()) {
            builder.append("  \"");
            builder.append(edge.nodeU());
            builder.append("\" -> \"");
            builder.append(edge.nodeV());
            builder.append("\";");
            builder.append(System.lineSeparator());
        }

        builder.append('}');
        return builder.toString();
    }
}
