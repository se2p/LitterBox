/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.SliceProfile;

import java.util.*;

public class InterproceduralSliceCoverage<T extends ASTNode> implements MetricExtractor<T> {
    public static final String NAME = "interprocedural_slice_coverage";

    @Override
    public double calculateMetric(T node) {
        Map<Defineable, Double> coverageMap = new HashMap<>();

        node.accept(new ScratchVisitor() {

            @Override
            public void visit(ActorDefinition actorDefinition) {
                for (Script script : actorDefinition.getScripts().getScriptList()) {
                    ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(actorDefinition);
                    script.accept(visitor);
                    ControlFlowGraph cfg = visitor.getControlFlowGraph();
                    ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
                    SliceProfile sliceProfile = new SliceProfile(pdg);
                    Map<Defineable, Set<Stmt>> profileMap = sliceProfile.getProfileMap();
                    for (Map.Entry<Defineable, Set<Stmt>> entry : profileMap.entrySet()) {
                        Defineable defineable = entry.getKey();
                        Set<Stmt> slice = entry.getValue();
                        int length = getScriptLength(pdg);
                        double coverage = length > 0 ? slice.size() / (double) getScriptLength(pdg) : 0.0;
                        if (!coverageMap.containsKey(defineable)) {
                            coverageMap.put(defineable, coverage);
                        } else {
                            coverageMap.put(defineable, coverageMap.get(defineable) + coverage);
                        }
                    }
                }

            }
        });

        if (coverageMap.isEmpty()) {
            return 0.0;
        }
        double coverage = coverageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        return coverage / coverageMap.size();
    }

    private int getScriptLength(ProgramDependenceGraph pdg) {
        return (int) pdg.getNodes().stream()
                .map(CFGNode::getASTNode)
                .filter(Stmt.class::isInstance)
                .count();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
