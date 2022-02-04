package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
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
                    for (Defineable defineable : profileMap.keySet()) {
                        Set<Stmt> slice = profileMap.get(defineable);
                        double coverage = slice.size() / getScriptLength(pdg);
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
        return "interprocedural_slice_coverage";
    }
}
