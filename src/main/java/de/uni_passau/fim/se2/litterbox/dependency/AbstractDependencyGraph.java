package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractDependencyGraph {

    protected MutableGraph<CFGNode> graph;

    protected ControlFlowGraph cfg;

    public AbstractDependencyGraph(ControlFlowGraph cfg) {
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
        cfg.getNodes().forEach(node -> graph.addNode(node));
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

    public Set<CFGNode> getNodes() {
        return Collections.unmodifiableSet(graph.nodes());
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

    public Optional<CFGNode> getNode(ASTNode node) {
        return graph.nodes().stream().filter(n -> n.getASTNode() == node).findFirst();
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

        builder.append("}");
        return builder.toString();
    }
}
