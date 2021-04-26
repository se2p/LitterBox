package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

public class CategoryEntropy<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "category_entropy";

    private int program_count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        program_count = 0;
        node.accept(this);
        return -program_count;
    }

    @Override
    public void visit(Script node) {
        ArrayList<MetricExtractor<Script>> list = new ArrayList<>();
        list.add(new EventsBlockCount<>()); //TODO if you doesnt want to count events as kind of blocks
                                            // add corner case for count_different_blocks == 0
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());
        list.add(new OperatorsBlockCount<>());


        double count = new BlockCount<Script>().calculateMetric(node);
        double local_entropy = 0.0; // Compute script category entropy

        for(MetricExtractor extractor : list) {
            double p_x = extractor.calculateMetric(node) / count;
            double category_entropy = p_x * (Math.log(p_x)/Math.log(2.0));
            local_entropy += category_entropy;
        }

        this.program_count += local_entropy;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        ArrayList<MetricExtractor<ProcedureDefinition>> list = new ArrayList<>();
        list.add(new EventsBlockCount<>());
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());
        list.add(new OperatorsBlockCount<>());


        double count = new BlockCount<ProcedureDefinition>().calculateMetric(node);
        double local_entropy = 0.0; // Compute script category entropy

        for(MetricExtractor extractor : list) {
            double p_x =  extractor.calculateMetric(node) / count;
            double category_entropy = p_x * (Math.log(p_x)/Math.log(2.0));
            local_entropy += category_entropy;
        }

        this.program_count += local_entropy;

    }

    @Override
    public String getName() {
        return NAME;
    }
}
