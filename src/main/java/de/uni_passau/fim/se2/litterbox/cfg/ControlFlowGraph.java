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
package de.uni_passau.fim.se2.litterbox.cfg;

import com.google.common.graph.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.AttributeAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlFlowGraph {

    private MutableGraph<CFGNode> graph;

    private SpecialNode entryNode = new SpecialNode("Entry");

    private SpecialNode exitNode = new SpecialNode("Exit");

    private int nextNodeId = 0;

    public ControlFlowGraph() {
        graph = GraphBuilder.directed().allowsSelfLoops(true).build();

        addNodeAndSetIndex(entryNode);
        addNodeAndSetIndex(exitNode);
    }

    private ControlFlowGraph(MutableGraph<CFGNode> graph) {
        this.graph = graph;
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
        addNodeAndSetIndex(node);
        return node;
    }

    public EventNode addNode(Event node, ActorDefinition actor, ASTNode scriptOrProcedure) {
        EventNode cfgNode = new EventNode(node, actor, scriptOrProcedure);
        addNodeAndSetIndex(cfgNode);
        return cfgNode;
    }

    private void addNodeAndSetIndex(CFGNode cfgNode) {
        cfgNode.setId(nextNodeId++);
        graph.addNode(cfgNode);
    }

    public MessageNode addNode(Message message, ASTNode scriptOrProcedure) {
        MessageNode cfgNode = new MessageNode(message, scriptOrProcedure);
        addNodeAndSetIndex(cfgNode);
        return cfgNode;
    }

    public AttributeEventNode addNode(AttributeAboveValue node, ActorDefinition actor, ASTNode scriptOrProcedure) {
        AttributeEventNode cfgNode = new AttributeEventNode(node, actor, scriptOrProcedure);
        addNodeAndSetIndex(cfgNode);
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
        return toDotString(node -> node.toString());
    }

    public String toDotString(Function<CFGNode, String> labelProvider) {
        StringBuilder builder = new StringBuilder();

        builder.append("digraph {");
        builder.append(System.lineSeparator());

        for (CFGNode node : graph.nodes()) {
            builder.append("  \"");
            builder.append(node);
            builder.append("\" [label=\"");
            builder.append(escapeDotLabel(labelProvider.apply(node)));
            builder.append("\"];");
            builder.append(System.lineSeparator());
        }

        for (EndpointPair<CFGNode> edge : graph.edges()) {
            builder.append("  \"");
            builder.append(edge.nodeU());
            builder.append("\" -> \"");
            builder.append(edge.nodeV());
            builder.append("\" [penwidth=2];");
            builder.append(System.lineSeparator());
        }

        builder.append('}');
        return builder.toString();
    }

    private String escapeDotLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.replace("\"", "\\\"").replace("\n", "\\n");
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

    public ControlFlowGraph reverseAndFilterInterproceduralDependencies() {
        ControlFlowGraph newCFG = new ControlFlowGraph();
        newCFG.graph = Graphs.copyOf(Graphs.transpose(processNonDependentInterproceduralEdges(graph)));
        newCFG.entryNode = this.exitNode;
        newCFG.exitNode = this.entryNode;

        return newCFG;
    }

    private MutableGraph<CFGNode> processNonDependentInterproceduralEdges(MutableGraph<CFGNode> graph) {
        MutableGraph<CFGNode> processedGraph = Graphs.copyOf(graph);
        Set<Pair<CFGNode>> toRemove = new HashSet<>();

        for (CFGNode node : processedGraph.nodes()) {
            // Avoid clone nodes from becoming control dependencies in the local script
            if (node.getASTNode() instanceof CreateCloneOf) {
                Set<CFGNode> successors = processedGraph.successors(node);
                for (CFGNode suc : successors) {
                    if (suc instanceof CloneEventNode) {
                        toRemove.add(new Pair<>(node, suc));
                    }
                }
            }

            // Avoid broadcast nodes from becoming control dependencies in the local script
            // (does not apply to BroadcastAndWait)
            if (node.getASTNode() instanceof Broadcast) {
                Set<CFGNode> successors = processedGraph.successors(node);
                for (CFGNode suc : successors) {
                    if (suc instanceof MessageNode) {
                        toRemove.add(new Pair<>(node, suc));
                    }
                }
            }
        }

        for (Pair<CFGNode> p : toRemove) {
            processedGraph.removeEdge(p.getFst(), p.getSnd());
        }
        return processedGraph;
    }

    public ControlFlowGraph reverse() {
        ControlFlowGraph newCFG = new ControlFlowGraph();
        newCFG.graph = Graphs.copyOf(Graphs.transpose(graph));
        newCFG.entryNode = this.exitNode;
        newCFG.exitNode = this.entryNode;
        return newCFG;
    }

    public static List<Stmt> getStatementsInOrder(StmtList stmtList) {
        List<Stmt> result = new ArrayList<>();
        collectStmts(stmtList, result);
        return result;
    }

    public static List<Stmt> getStatementsInReverseOrder(StmtList stmtList) {
        List<Stmt> result = getStatementsInOrder(stmtList);
        Collections.reverse(result);
        return result;
    }

    private static void collectStmts(StmtList stmtList, List<Stmt> result) {
        for (Stmt stmt : stmtList.getStmts()) {
            result.add(stmt);
            if (stmt instanceof LoopStmt loop) {
                collectStmts(loop.getStmtList(), result);
            } else if (stmt instanceof IfThenStmt ifThen) {
                collectStmts(ifThen.getThenStmts(), result);
            } else if (stmt instanceof IfElseStmt ifElse) {
                collectStmts(ifElse.getThenStmts(), result);
                collectStmts(ifElse.getElseStmts(), result);
            }
        }
    }

    public void ensureAllNodesReachableFromEntry() {
        Set<CFGNode> reachable = new HashSet<>();
        List<CFGNode> queue = new ArrayList<>();
        queue.add(entryNode);
        reachable.add(entryNode);

        int head = 0;
        while (head < queue.size()) {
            CFGNode current = queue.get(head++);
            for (CFGNode successor : graph.successors(current)) {
                if (reachable.add(successor)) {
                    queue.add(successor);
                }
            }
        }

        for (CFGNode node : graph.nodes()) {
            if (!reachable.contains(node)) {
                graph.putEdge(entryNode, node);
                // Continue BFS from the newly connected node to find its reachable subgraph
                // This avoids adding edges to every single node in a disconnected component, just the root we found
                reachable.add(node);
                queue.add(node);
                while (head < queue.size()) {
                    CFGNode current = queue.get(head++);
                    for (CFGNode successor : graph.successors(current)) {
                        if (reachable.add(successor)) {
                            queue.add(successor);
                        }
                    }
                }
            }
        }
    }
}
