package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BinaryRankTournamentTest {
    Random mockedRandom;
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<Integer> productions;
    List<RefactoringFinder> refactoringFinders;

    RefactorSequence testSuite1;
    RefactorSequence testSuite2;

    List<RefactorSequence> population;

    Selection<RefactorSequence> selection;

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties();

        mockedRandom = mock(Random.class);
        mutation = mock(RefactorSequenceMutation.class);
        crossover = mock(RefactorSequenceCrossover.class);
        productions = List.of(1, 1, 1);
        refactoringFinders = List.of();

        testSuite1 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);
        testSuite2 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);

        population = Lists.newArrayList(testSuite1, testSuite2);

        selection = new BinaryRankTournament(mockedRandom);
    }

    @Test
    void testSelectionByRank() {
        testSuite1.setRank(0);
        testSuite2.setRank(1);

        when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(testSuite1, candidate1);
        assertEquals(testSuite1, candidate1);

        testSuite1.setRank(2);
        when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(testSuite2, candidate2);
        assertEquals(testSuite2, candidate2);
    }

    @Test
    void testSelectionByDistance() {
        testSuite1.setRank(0);
        testSuite2.setRank(0);
        testSuite1.setDistance(0.0);
        testSuite2.setDistance(1.0);
        when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1);

        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(testSuite2, candidate1);
        assertEquals(testSuite2, candidate1);

        testSuite1.setDistance(2.0);
        when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(testSuite1, candidate1);
        assertEquals(testSuite1, candidate1);
    }

    @Test
    void testNeverTheSameBothArePicked() {
        testSuite1.setRank(0);
        testSuite2.setRank(1);
        when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(0).thenReturn(1);
        RefactorSequence candidate = selection.apply(population);
        assertNotSame(testSuite2, candidate);
        assertEquals(testSuite2, candidate);
    }
}
