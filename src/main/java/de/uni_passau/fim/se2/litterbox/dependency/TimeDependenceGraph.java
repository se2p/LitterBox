package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.graph.MutableGraph;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.TransferFunction;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class TimeDependenceGraph extends AbstractDependencyGraph {

    public TimeDependenceGraph(ControlFlowGraph cfg) {
        super(cfg);
    }

    @Override
    protected MutableGraph<CFGNode> computeGraph() {

        DataflowAnalysisBuilder<Delay> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Delay> analysis = builder.withForward().withMay().withTransferFunction(new DelayTransferFunction()).build();
        analysis.applyAnalysis();

        MutableGraph<CFGNode> tdg = createUnconnectedGraph();

        for (CFGNode node : cfg.getNodes()) {
            for (Delay delay : analysis.getDataflowFacts(node)) {
                tdg.putEdge(delay.getSource(), node);
            }
        }

        return tdg;
    }

    private class Delay implements DataflowFact {

        private CFGNode source;

        public Delay(CFGNode source) {
            this.source = source;
        }

        public CFGNode getSource() {
            return source;
        }

        public void setSource(CFGNode source) {
            this.source = source;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Delay)) return false;
            Delay delay = (Delay) o;
            return Objects.equals(source, delay.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source);
        }
    }

    private class DelayTransferFunction implements TransferFunction<Delay> {

        @Override
        public Set<Delay> apply(CFGNode node, Set<Delay> inFacts) {
            // There is no kill set, so the result is inFacts ∪ {gen}
            Set<Delay> result = new LinkedHashSet<>(inFacts);

            if (node.getASTNode() != null && node.getASTNode() instanceof TimedStmt) {
                result.add(new Delay(node));
            }

            return result;
        }
    }
}
