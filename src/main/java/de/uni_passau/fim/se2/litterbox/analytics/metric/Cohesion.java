package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

public class Cohesion <T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "cohesion";

    private int count = 0;

    private double local_cohesion = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        ArrayList<MetricExtractor<Script>> list = new ArrayList<>();
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());

        int count_different_blocks = 0;

        for(MetricExtractor extractor : list) {
            double count = extractor.calculateMetric(node);
            if(count > 0)
                count_different_blocks++;
        }

        // Calculate local script cohesion
        local_cohesion = new BlockCount<Script>().calculateMetric(node) / count_different_blocks;

        count += local_cohesion;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        ArrayList<MetricExtractor<ProcedureDefinition>> list = new ArrayList<>();
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());

        int count_different_blocks = 0;

        for(MetricExtractor extractor : list) {
            double count = extractor.calculateMetric(node);
            if(count > 0)
                count_different_blocks++;
        }

        // Calculate local script cohesion
        local_cohesion = new BlockCount<ProcedureDefinition>().calculateMetric(node) / count_different_blocks;

        count += local_cohesion;

    }

    @Override
    public String getName() {
        return NAME;
    }
}
