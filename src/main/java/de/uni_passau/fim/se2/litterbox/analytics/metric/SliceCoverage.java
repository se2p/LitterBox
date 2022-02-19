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
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor(actor);
        node.accept(visitor);
        ControlFlowGraph cfg = visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);
        return sliceProfile.getCoverage();
    }

    @Override
    public String getName() {
        return "slice_coverage";
    }
}
