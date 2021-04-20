package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefactorSequenceGeneratorTest {

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties();
    }

    @Test
    void generateNewRefactorSequence() {
        Random mockedRandom = mock(Random.class);
        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);

        List<Integer> expectedProductions = List.of(1, 2);

        // returning 1 means 2 ints will be included since the formula uses (1 + random Number) to avoid 0
        when(mockedRandom.nextInt(10)).thenReturn(1);

        // include the integers 1 and 2
        when(mockedRandom.nextInt(255)).thenReturn(1).thenReturn(2);
        RefactorSequenceGenerator generator = new RefactorSequenceGenerator(mutation, crossover, mockedRandom, List.of());
        RefactorSequence generated = generator.get();

        assertEquals(2, generated.getProductions().size());
        assertEquals(expectedProductions, generated.getProductions());
        assertSame(mutation, generated.getMutation());
        assertSame(crossover, generated.getCrossover());
    }
}
