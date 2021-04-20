package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RefactorSequenceGenerator implements ChromosomeGenerator<RefactorSequence> {

    private static final int MAX_PRODUCTIONS_PER_SOLUTION = Integer.parseInt(System.getProperty("nsga-ii.maxProductionsPerSolution"));
    private static final int MAX_PRODUCTION_NUMBER = Integer.parseInt(System.getProperty("nsga-ii.maxProductionNumber"));

    private final Mutation<RefactorSequence> mutation;
    private final Crossover<RefactorSequence> crossover;
    private final Random random;
    private final List<RefactoringFinder> refactoringFinders;

    public RefactorSequenceGenerator(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                                     Random random, List<RefactoringFinder> refactoringFinders) {
        this.mutation = mutation;
        this.crossover = crossover;
        this.random = random;
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * Creates and returns a random chromosome that represents a valid and admissible solution for the problem at hand.
     *
     * @return a random chromosome
     */
    @Override
    public RefactorSequence get() {
        List<Integer> productions = new LinkedList<>();
        int numberOfProductions = 1 + random.nextInt(MAX_PRODUCTIONS_PER_SOLUTION);
        for (int i = 0; i < numberOfProductions; i++) {
            productions.add(random.nextInt(MAX_PRODUCTION_NUMBER));
        }
        return new RefactorSequence(mutation, crossover, productions, refactoringFinders);
    }
}
