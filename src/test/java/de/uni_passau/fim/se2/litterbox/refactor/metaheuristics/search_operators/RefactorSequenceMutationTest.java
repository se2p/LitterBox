package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefactorSequenceMutationTest {

    Random mockedRandom;
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<RefactoringFinder> refactoringFinders;

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");

        mockedRandom = mock(Random.class);
        refactoringFinders = List.of();
        mutation = new RefactorSequenceMutation(mockedRandom, refactoringFinders);
        crossover = mock(RefactorSequenceCrossover.class);
    }

    @Test
    void testMutation() {

        final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");

        List<Integer> productions = List.of(0, 0, 0, 0);
        RefactorSequence parent = new RefactorSequence(mutation, crossover, productions, refactoringFinders);

        when(mockedRandom.nextDouble()).thenReturn(1d).thenReturn(0d); // do not mutate 0th element, but everything afterwards
        when(mockedRandom.nextInt(3)).thenReturn(0).thenReturn(1).thenReturn(2);
        when(mockedRandom.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS)).thenReturn(13).thenReturn(17);

        RefactorSequence mutant = parent.mutate();
        assertEquals(4, mutant.getProductions().size());
        assertEquals(0, mutant.getProductions().get(0));
        assertEquals(13, mutant.getProductions().get(1));
        assertEquals(0, mutant.getProductions().get(2));
        assertEquals(17, mutant.getProductions().get(3));
    }
}
