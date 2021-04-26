package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;

class BinaryRankTournamentTest {
    // Random mockedRandom; TODO
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<Integer> productions;
    List<RefactoringFinder> refactoringFinders;

    RefactorSequence refactorSequence1;
    RefactorSequence refactorSequence2;

    List<RefactorSequence> population;

    Selection<RefactorSequence> selection;

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");

        // mockedRandom = mock(Random.class); TODO
        mutation = mock(RefactorSequenceMutation.class);
        crossover = mock(RefactorSequenceCrossover.class);
        productions = List.of(1, 1, 1);
        refactoringFinders = List.of();

        refactorSequence1 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);
        refactorSequence2 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);

        population = Lists.newArrayList(refactorSequence1, refactorSequence2);

        // selection = new BinaryRankTournament(mockedRandom);
        selection = new BinaryRankTournament(); // TODO
    }

    @Test
    void testSelectionByRank() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(1);

        // when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1); TODO
        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(refactorSequence1, candidate1);
        assertEquals(refactorSequence1, candidate1);

        refactorSequence1.setRank(2);
        // when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1); TODO
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(refactorSequence2, candidate2);
        assertEquals(refactorSequence2, candidate2);
    }

    @Test
    void testSelectionByDistance() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(0);
        refactorSequence1.setDistance(0.0);
        refactorSequence2.setDistance(1.0);
        // when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1); TODO

        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(refactorSequence2, candidate1);
        assertEquals(refactorSequence2, candidate1);

        refactorSequence1.setDistance(2.0);
        // when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(1); TODO
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(refactorSequence1, candidate1);
        assertEquals(refactorSequence1, candidate1);
    }

    @Test
    void testNeverTheSameBothArePicked() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(1);
        // when(mockedRandom.nextInt(2)).thenReturn(0).thenReturn(0).thenReturn(1); TODO
        RefactorSequence candidate = selection.apply(population);
        assertNotSame(refactorSequence2, candidate);
        assertEquals(refactorSequence2, candidate);
    }
}
