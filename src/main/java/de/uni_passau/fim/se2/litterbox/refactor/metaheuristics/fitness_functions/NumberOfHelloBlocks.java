package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.ControlBlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.SayHelloBlockCount;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class NumberOfHelloBlocks implements MinimizingFitnessFunction<RefactorSequence> {

    private final Program program;

    public NumberOfHelloBlocks(Program program) {
        this.program = program;
    }

    /**
     * <p>
     * Computes and returns the fitness value of the given solution {@code c}.
     *
     * @param refactorSequence the solution to rate
     * @return the fitness value of the given solution
     * @throws NullPointerException if {@code null} is given
     */
    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.applyToProgram(program);

        SayHelloBlockCount helloCounter = new SayHelloBlockCount();

        return helloCounter.calculateMetric(refactoredProgram);
    }
}
