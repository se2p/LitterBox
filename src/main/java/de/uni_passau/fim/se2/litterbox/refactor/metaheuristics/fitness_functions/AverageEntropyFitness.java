package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import static java.util.stream.Collectors.*;

import de.uni_passau.fim.se2.litterbox.analytics.metric.CategoryEntropy;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AverageEntropyFitness implements MinimizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "average_entropy_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        List<Double> entropies = new ArrayList<>();

        refactoredProgram.accept(new ScratchVisitor() {
            @Override
            public void visit(Script node) {
                CategoryEntropy<Script> entropy = new CategoryEntropy<>();
                entropies.add(entropy.calculateMetric(node));
            }
        });

        return entropies.stream().collect(averagingDouble(Number::doubleValue));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
