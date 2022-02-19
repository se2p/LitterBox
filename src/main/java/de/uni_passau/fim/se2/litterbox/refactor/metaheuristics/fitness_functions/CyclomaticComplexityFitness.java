package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.InterproceduralCyclomaticComplexity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class CyclomaticComplexityFitness implements MinimizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "cyclomatic_complexity_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        InterproceduralCyclomaticComplexity<Program> cyclomaticComplexity = new InterproceduralCyclomaticComplexity<>();
        return cyclomaticComplexity.calculateMetric(refactoredProgram);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
