package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OffspringGeneratorTest {
    MockedStatic<Randomness> mockedRandomness;

    Program program;

    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;

    List<Integer> production1;
    List<Integer> production2;
    List<Integer> production3;
    List<Integer> production4;

    List<RefactoringFinder> refactoringFinders;

    RefactorSequence parent1;
    RefactorSequence parent2;
    RefactorSequence mutant1;
    RefactorSequence mutant2;

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("litterbox.properties");

        mockedRandomness = Mockito.mockStatic(Randomness.class);

        program = mock(Program.class);
        when(program.deepCopy()).thenReturn(program);

        mutation = mock(RefactorSequenceMutation.class);
        crossover = mock(RefactorSequenceCrossover.class);

        production1 = List.of(1, 1, 1);
        production2 = List.of(2, 2, 2);
        production3 = List.of(3, 3, 3);
        production4 = List.of(4, 4, 4);

        refactoringFinders = List.of();

        parent1 = new RefactorSequence(program, mutation, crossover, production1, refactoringFinders);
        parent2 = new RefactorSequence(program, mutation, crossover, production2, refactoringFinders);
        mutant1 = new RefactorSequence(program, mutation, crossover, production3, refactoringFinders);
        mutant2 = new RefactorSequence(program, mutation, crossover, production4, refactoringFinders);
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }

    @Test
    void offspringGeneratorCreatesOffspringCorrectlyWithCrossover() {
        BinaryRankTournament<RefactorSequence> mockedSelection = mock(BinaryRankTournament.class);

        List<RefactorSequence> generation0 = List.of(parent1, parent2);
        when(mockedSelection.apply(generation0)).thenReturn(parent1).thenReturn(parent2);
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.5);
        when(crossover.apply(parent1, parent2)).thenReturn(Pair.of(parent1, parent2));

        when(mutation.apply(parent1)).thenReturn(mutant1);
        when(mutation.apply(parent2)).thenReturn(mutant2);

        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(mockedSelection);
        List<RefactorSequence> generation1 = offspringGenerator.generateOffspring(generation0);
        assertEquals(2, generation1.size());
        assertEquals(mutant1, generation1.get(0));
        assertEquals(mutant2, generation1.get(1));
    }

    @Test
    void offspringGeneratorCreatesOffspringCorrectlyWithoutCrossover() {
        BinaryRankTournament<RefactorSequence> mockedSelection = mock(BinaryRankTournament.class);

        List<RefactorSequence> generation0 = List.of(parent1, parent2);
        when(mockedSelection.apply(generation0)).thenReturn(parent1).thenReturn(parent2);
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.9);

        when(mutation.apply(parent1)).thenReturn(mutant1);
        when(mutation.apply(parent2)).thenReturn(mutant2);

        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(mockedSelection);
        List<RefactorSequence> generation1 = offspringGenerator.generateOffspring(generation0);
        assertEquals(2, generation1.size());
        assertEquals(mutant1, generation1.get(0));
        assertEquals(mutant2, generation1.get(1));
    }

    @Test
    void offspringGeneratorCreatesUnevenPopulationSize() {
        BinaryRankTournament<RefactorSequence> mockedSelection = mock(BinaryRankTournament.class);

        List<RefactorSequence> generation0 = List.of(parent1);
        when(mockedSelection.apply(generation0)).thenReturn(parent1).thenReturn(parent2);
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.5);
        when(crossover.apply(parent1, parent2)).thenReturn(Pair.of(parent1, parent2));

        when(mutation.apply(parent1)).thenReturn(mutant1);
        when(mutation.apply(parent2)).thenReturn(mutant2);

        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0);

        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(mockedSelection);
        List<RefactorSequence> generation1 = offspringGenerator.generateOffspring(generation0);
        assertEquals(1, generation1.size());
        assertSame(mutant2, generation1.get(0));
    }
}
