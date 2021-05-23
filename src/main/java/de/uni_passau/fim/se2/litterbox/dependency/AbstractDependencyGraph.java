package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

import java.util.*;
import java.util.stream.Collectors;

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

    public Set<CFGNode> getCFGNodes(Set<ASTNode> astNodes) {
        return graph.nodes().stream().filter(n -> astNodes.contains(n.getASTNode())).collect(Collectors.toSet());
    }

    public boolean hasDependencyEdge(Collection<Stmt> sourceNodes, Collection<Stmt> targetNodes) {
        Set<ASTNode> transitiveSourceNodes = new LinkedHashSet<>();
        sourceNodes.forEach(s -> transitiveSourceNodes.addAll(getTransitiveStatements(s)));

        Set<ASTNode> transitiveTargetNodes = new LinkedHashSet<>();
        targetNodes.forEach(s -> transitiveTargetNodes.addAll(getTransitiveStatements(s)));

        return hasAnyEdge(getCFGNodes(transitiveSourceNodes), getCFGNodes(transitiveTargetNodes));
    }

    private boolean hasAnyEdge(Set<CFGNode> sourceNodes, Set<CFGNode> targetNodes) {
        for (EndpointPair<CFGNode> edge : getEdges()) {
            if (sourceNodes.contains(edge.nodeU()) && targetNodes.contains(edge.nodeV())) {
                return true;
            }
        }
        return false;
    }

    private Set<Stmt> getTransitiveStatements(Stmt stmt) {
        return getTransitiveNodes(stmt).stream().filter(node -> node instanceof Stmt).map(Stmt.class::cast).collect(Collectors.toSet());
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

        builder.append("}");
        return builder.toString();
    }
}
