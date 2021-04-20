package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RefactorSequenceMutation implements Mutation<RefactorSequence> {

    private static final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.numberOfProductions");

    private final Random random;
    private final List<RefactoringFinder> refactoringFinders;

    public RefactorSequenceMutation(Random random, List<RefactoringFinder> refactoringFinders) {
        this.random = random;
        this.refactoringFinders = refactoringFinders;
    }

    @Override
    public RefactorSequence apply(RefactorSequence refactorSequence) {

        List<Integer> productions = refactorSequence.getProductions();

        // TODO add/ remove or swap a random number based on size of current production list
        List<Integer> mutatedProductions = new LinkedList<>(productions);
        mutatedProductions.add(random.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS)); // currently just add random numbers until T0D0 is fixed

        return new RefactorSequence(refactorSequence.getMutation(), refactorSequence.getCrossover(), mutatedProductions, refactoringFinders);
    }
}
