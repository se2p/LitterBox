package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.SliceCoverage;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.averagingDouble;

public class AverageCohesionFitness implements MaximizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "average_cohesion_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        List<Double> coverage = new ArrayList<>();

        refactoredProgram.accept(new ScratchVisitor() {
            private ActorDefinition actor = null;
            @Override
            public void visit(ActorDefinition actorDefinition) {
                this.actor = actorDefinition;
            }
            @Override
            public void visit(Script node) {
                SliceCoverage<Script> sliceCoverage = new SliceCoverage<>();
                coverage.add(sliceCoverage.calculateMetric(node));
            }
        });

        return coverage.stream().collect(averagingDouble(Number::doubleValue));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
