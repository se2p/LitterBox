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
package de.uni_passau.fim.se2.litterbox.cfg;

import com.google.common.graph.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlFlowGraph {

    private MutableGraph<CFGNode> graph;

    private SpecialNode entryNode = new SpecialNode("Entry");

    private SpecialNode exitNode = new SpecialNode("Exit");

    public ControlFlowGraph() {
        graph = GraphBuilder.directed().allowsSelfLoops(true).build();

        graph.addNode(entryNode);
        graph.addNode(exitNode);
    }

    public int getNumNodes() {
        return graph.nodes().size();
    }

    public int getNumEdges() {
        return graph.edges().size();
    }

    public CFGNode getEntryNode() {
        return entryNode;
    }

    public CFGNode getExitNode() {
        return exitNode;
    }

    public Set<CFGNode> getNodes() {
        return Collections.unmodifiableSet(graph.nodes());
    }

    public Set<EndpointPair<CFGNode>> getEdges() {
        return Collections.unmodifiableSet(graph.edges());
    }

    public Optional<CFGNode> getNode(ASTNode node) {
        return graph.nodes().stream().filter(n -> n.getASTNode() == node).findFirst();
    }

    public Iterable<CFGNode> getNodesInPostOrder() {
        return Traverser.forGraph(graph).depthFirstPostOrder(entryNode);
    }

    public Iterable<CFGNode> getNodesInReversePostOrder() {
        return Traverser.forGraph(graph).depthFirstPreOrder(entryNode);
    }

    public Set<CFGNode> getSuccessors(CFGNode node) {
        return graph.successors(node);
    }

    public Set<CFGNode> getPredecessors(CFGNode node) {
        return graph.predecessors(node);
    }

    public StatementNode addNode(Stmt stmt, ActorDefinition actor, ASTNode scriptOrProcedure) {
        StatementNode node = new StatementNode(stmt, actor, scriptOrProcedure);
        graph.addNode(node);
        return node;
    }

    public EventNode addNode(Event node) {
        EventNode cfgNode = new EventNode(node);
        graph.addNode(cfgNode);
        return cfgNode;
    }

    public MessageNode addNode(Message message) {
        MessageNode cfgNode = new MessageNode(message);
        graph.addNode(cfgNode);
        return cfgNode;
    }

    public AttributeEventNode addNode(AttributeAboveValue node, ActorDefinition actor) {
        AttributeEventNode cfgNode = new AttributeEventNode(node, actor);
        graph.addNode(cfgNode);
        return cfgNode;
    }

    public void addEdge(CFGNode from, CFGNode to) {
        graph.putEdge(from, to);
    }

    public void addEdgeFromEntry(CFGNode node) {
        graph.putEdge(entryNode, node);
    }

    public void addEdgeToExit(CFGNode node) {
        graph.putEdge(node, exitNode);
    }

    public void fixDetachedEntryExit() {
        if (graph.degree(entryNode) == 0) {
            graph.putEdge(entryNode, exitNode);
        }
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

        builder.append("}");
        return builder.toString();
    }

    public Map<String, List<String>> getFlowEdges() {
        Map<String, List<String>> flowEdges = new HashMap<>();
        for (EndpointPair<CFGNode> edge : graph.edges()) {
            String[] nodeUTexts = edge.nodeU().toString().split("\\.");
            String nodeU = nodeUTexts[nodeUTexts.length - 1].split("\\@")[0];
            String[] nodeVTexts = edge.nodeV().toString().split("\\.");
            String nodeV = nodeVTexts[nodeVTexts.length - 1].split("\\@")[0];

            if (!"entry".equalsIgnoreCase(nodeU) && !"exit".equalsIgnoreCase(nodeV)) {
                flowEdges.computeIfAbsent(nodeU, k -> new ArrayList<>()).add(nodeV);
            }
        }
        return flowEdges;
    }

    public Set<Definition> getDefinitions() {
        return graph.nodes().stream().map(CFGNode::getDefinitions).flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Set<Use> getUses() {
        return graph.nodes().stream().map(CFGNode::getUses).flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Stream<CFGNode> stream() {
        return graph.nodes().stream();
    }

    public Iterable<CFGNode> traverse() {
        return Traverser.forGraph(graph).breadthFirst(entryNode);
    }

    public ControlFlowGraph reverse() {
        ControlFlowGraph newCFG = new ControlFlowGraph();
        newCFG.graph = Graphs.copyOf(Graphs.transpose(graph));
        newCFG.entryNode = this.exitNode;
        newCFG.exitNode = this.entryNode;

        return newCFG;
    }
}
