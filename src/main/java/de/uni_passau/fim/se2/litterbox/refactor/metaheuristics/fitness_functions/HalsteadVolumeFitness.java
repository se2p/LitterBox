package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.HalsteadVolume;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class HalsteadVolumeFitness implements MinimizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "halstead_volume_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        HalsteadVolume volume = new HalsteadVolume();
        return volume.calculateMetric(refactoredProgram);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
