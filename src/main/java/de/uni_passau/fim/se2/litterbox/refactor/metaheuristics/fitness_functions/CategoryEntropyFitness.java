package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.CategoryEntropy;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.LinkedHashMap;

public class CategoryEntropyFitness implements MinimizingFitnessFunction<RefactorSequence>{

    private Program program;
    private LinkedHashMap<RefactorSequence, Double> cache = new LinkedHashMap<>();

    public CategoryEntropyFitness(Program program) {
        this.program = program;
    }

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        if(cache.containsKey(refactorSequence)) {
            return cache.get(refactorSequence);
        } else {
            Program refactoredProgram = refactorSequence.getRefactoredProgram();

            CategoryEntropy entropy = new CategoryEntropy();
            double fitness = entropy.calculateMetric(refactoredProgram);

            cache.put(refactorSequence, fitness);

            return fitness;
        }
    }
}
