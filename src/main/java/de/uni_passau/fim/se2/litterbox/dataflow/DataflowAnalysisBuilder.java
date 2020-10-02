/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.dataflow;

import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.DataflowFact;

import java.util.Set;

public class DataflowAnalysisBuilder<T extends DataflowFact> {

    private final DataflowAnalysis<T> analysis;

    public DataflowAnalysisBuilder(ControlFlowGraph cfg) {
        analysis = new DataflowAnalysis<>(cfg);
    }

    public DataflowAnalysisBuilder<T> withMay() {
        analysis.setJoinFunction(new MayFunction<T>());
        analysis.initializeMay();
        return this;
    }

    public DataflowAnalysisBuilder<T> withMust(Set<T> allFacts) {
        analysis.setJoinFunction(new MustFunction<T>());
        analysis.initializeMust(allFacts); // TODO: Need set of all facts!
        return this;
    }

    public DataflowAnalysisBuilder<T> withTransferFunction(TransferFunction<T> t) {
        analysis.setTransferFunction(t);
        return this;
    }

    public DataflowAnalysisBuilder<T> withForward() {
        analysis.setFlowDirection(new ForwardFlowDirection());
        return this;
    }

    public DataflowAnalysisBuilder<T> withBackward() {
        analysis.setFlowDirection(new BackwardFlowDirection());
        return this;
    }

    public DataflowAnalysis<T> build() {
        return analysis;
    }
}
