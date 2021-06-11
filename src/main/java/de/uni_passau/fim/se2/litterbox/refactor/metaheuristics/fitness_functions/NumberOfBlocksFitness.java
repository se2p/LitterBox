package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class NumberOfBlocksFitness implements MinimizingFitnessFunction<RefactorSequence>{
    private static final String NAME = "number_of_blocks_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        BlockCount blockCountMetric = new BlockCount();
        return blockCountMetric.calculateMetric(refactoredProgram);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
