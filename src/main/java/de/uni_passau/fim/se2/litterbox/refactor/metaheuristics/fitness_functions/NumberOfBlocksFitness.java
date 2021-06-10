package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class NumberOfBlocksFitness implements MinimizingFitnessFunction<RefactorSequence>{
    private static final String NAME = "number_of_blocks_fitness";

    private Program program;

    public NumberOfBlocksFitness(Program program) {
        this.program = program;
    }

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        BlockCount blockCountMetric = new BlockCount();
        double fitness = blockCountMetric.calculateMetric(refactoredProgram);
        double normalizedFitness = fitness / (1 + fitness);

        return normalizedFitness;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
