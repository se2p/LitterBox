package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.CategoryEntropy;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class CategoryEntropyFitness implements MinimizingFitnessFunction<RefactorSequence>{

    private static final String NAME = "category_entropy_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        CategoryEntropy entropy = new CategoryEntropy();
        return entropy.calculateMetric(refactoredProgram);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
