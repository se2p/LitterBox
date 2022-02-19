package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.ProgramDependenceGraph;
import de.uni_passau.fim.se2.litterbox.dependency.SliceProfile;

public class SliceTightness<T extends ASTNode> implements MetricExtractor<T> {

    @Override
    public double calculateMetric(T node) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        node.accept(visitor);
        ControlFlowGraph cfg =  visitor.getControlFlowGraph();
        ProgramDependenceGraph pdg = new ProgramDependenceGraph(cfg);
        SliceProfile sliceProfile = new SliceProfile(pdg);
        return sliceProfile.getTightness();
    }

    @Override
    public String getName() {
        return "slice_tightness";
    }
}
