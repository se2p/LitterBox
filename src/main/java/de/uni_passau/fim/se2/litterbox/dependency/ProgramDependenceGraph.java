package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;

public class ProgramDependenceGraph extends AbstractDependencyGraph {

    public ProgramDependenceGraph(ControlFlowGraph cfg) {
        super(cfg);
    }

    public boolean hasDependency(CFGNode source, CFGNode target) {
        // TODO: Does this need to be done twice because of direction?
        // TODO: What about transitive dependencies?
        return graph.hasEdgeConnecting(source, target);
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
