package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

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
}
