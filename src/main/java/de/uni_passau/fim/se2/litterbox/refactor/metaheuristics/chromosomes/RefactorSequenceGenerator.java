package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.LinkedList;
import java.util.List;

public class RefactorSequenceGenerator implements ChromosomeGenerator<RefactorSequence> {

    private static final int MAX_REFACTORINGS_PER_SOLUTION = 1;

    private final Mutation<RefactorSequence> mutation;
    private final Crossover<RefactorSequence> crossover;
    private final Program program;
    private final List<RefactoringFinder> refactoringFinders;

    public RefactorSequenceGenerator(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover, Program program, List<RefactoringFinder> refactoringFinders) {
        this.mutation = mutation;
        this.crossover = crossover;
        this.program = program;
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * Creates and returns a random chromosome. Implementations must ensure that the returned
     * chromosome represents a valid and admissible solution for the problem at hand.
     *
     * @return a random chromosome
     */
    @Override
    public RefactorSequence get() {
        // TODO create actual random solution instead of empty and rely on mutation
        // start with an empty list for showcase, to test if mutation was rated better
        List<Refactoring> refactoringSequence = new LinkedList<>();
        return new RefactorSequence(mutation, crossover, refactoringSequence);
    }
}
