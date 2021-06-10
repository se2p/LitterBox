package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.HalsteadDifficulty;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;

public class SumComplexityFitness implements MinimizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "sum_complexity_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        List<Double> complexities = new ArrayList<>();

        refactoredProgram.accept(new ScratchVisitor() {
            @Override
            public void visit(Script node) {
                HalsteadDifficulty<Script> difficulty = new HalsteadDifficulty<>();
                complexities.add(difficulty.calculateMetric(node));
            }
        });

        return complexities.stream().mapToDouble(Number::doubleValue).sum();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
