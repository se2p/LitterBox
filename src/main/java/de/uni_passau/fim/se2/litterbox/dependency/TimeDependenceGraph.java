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

    private static class Delay implements DataflowFact {

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

    private static class DelayTransferFunction implements TransferFunction<Delay> {

        @Override
        public Set<Delay> apply(CFGNode node, Set<Delay> inFacts) {
            // There is no kill set, so the result is inFacts ∪ {gen}
            Set<Delay> result = new LinkedHashSet<>(inFacts);

            if (node.getASTNode() instanceof TimedStmt) {
                result.add(new Delay(node));
            }

            return result;
        }
    }
}
