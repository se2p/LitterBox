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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.SliceProfile;

public class SliceCoverage<T extends ASTNode> implements MetricExtractor<T> {

    private ActorDefinition actor;

    public SliceCoverage() {
        actor = null;
    }

    public SliceCoverage(ActorDefinition actor) {
        this.actor = actor;
    }

    @Override
    public double calculateMetric(T node) {
        ActorDefinition parent = actor;
        if (!(node instanceof ActorDefinition) && parent == null) {
            parent = findParentActor(node);
        }
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(parent);
        node.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);
        return sliceProfile.getCoverage();
    }

    private ActorDefinition findParentActor(T node) {
        ASTNode parent = node;
        while (!(parent instanceof ActorDefinition) && parent != null) {
            parent = parent.getParentNode();
        }
        return (ActorDefinition) parent;
    }

    @Override
    public String getName() {
        return "slice_coverage";
    }
}
