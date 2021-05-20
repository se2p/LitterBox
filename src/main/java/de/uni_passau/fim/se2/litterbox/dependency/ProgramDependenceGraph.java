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

    public Set<Stmt> backwardSlice(Collection<Stmt> target) {
        Set<CFGNode> targetNodes = target.stream().map(s -> getNode(s).get()).collect(Collectors.toSet());
        Set<CFGNode> slice = backwardSliceNodes(targetNodes);
        return slice.stream().filter(n -> n.getASTNode() != null & n.getASTNode() instanceof Stmt).map(n -> (Stmt) n.getASTNode()).collect(Collectors.toSet());
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

}
