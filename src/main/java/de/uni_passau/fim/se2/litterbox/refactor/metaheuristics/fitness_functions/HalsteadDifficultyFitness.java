package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.HalsteadDifficulty;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class HalsteadDifficultyFitness implements MinimizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "halstead_difficulty_fitness";

    private Program program;

    public HalsteadDifficultyFitness(Program program) {
        this.program = program;
    }

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        HalsteadDifficulty difficulty = new HalsteadDifficulty();
        double fitness = difficulty.calculateMetric(refactoredProgram);

        return fitness;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
