package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.ReachingDefinitionsTransferFunction;

public class DataDependenceGraph extends AbstractDependencyGraph {

    public DataDependenceGraph(ControlFlowGraph cfg) {
        super(cfg);
    }

    @Override
    protected MutableGraph<CFGNode> computeGraph() {

        DataflowAnalysisBuilder<Definition> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Definition> analysis = builder.withForward().withMay().withTransferFunction(new ReachingDefinitionsTransferFunction()).build();
        analysis.applyAnalysis();

        MutableGraph<CFGNode> ddg = createUnconnectedGraph();

        for (CFGNode node : cfg.getNodes()) {
            for (Use use : node.getUses()) {
                for (Definition def : analysis.getDataflowFacts(node)) {
                    if (use.getDefinable().equals(def.getDefinable())) {
                        CFGNode source = def.getDefinitionSource();
                        ddg.putEdge(source, node);
                    }
                }
            }
        }

        return ddg;
    }

}
