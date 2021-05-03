package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class NumberOfBlocksFitness implements MinimizingFitnessFunction<RefactorSequence>{

    private Program program;

    public NumberOfBlocksFitness(Program program) {
        this.program = program;
    }

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.applyToProgram(program);

        BlockCount blockCountMetric = new BlockCount();
        double fitness = blockCountMetric.calculateMetric(refactoredProgram);

        return fitness;
    }
}
