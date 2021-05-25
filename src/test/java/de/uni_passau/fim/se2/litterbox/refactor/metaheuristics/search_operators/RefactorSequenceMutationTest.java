package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RefactorSequenceMutationTest {
    MockedStatic<Randomness> mockedRandomness;
    Program program;
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<RefactoringFinder> refactoringFinders;

    @BeforeEach
    void setupEnv() {
        mockedRandomness = Mockito.mockStatic(Randomness.class);

        program = mock(Program.class);
        refactoringFinders = List.of();
        mutation = new RefactorSequenceMutation(refactoringFinders);
        crossover = mock(RefactorSequenceCrossover.class);
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }

    @Test
    void testMutation() {
        final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");
        mockedRandomness.when(Randomness::nextDouble).thenReturn(1d).thenReturn(0d); // do not mutate 0th element, but everything afterwards
        mockedRandomness.when(() -> Randomness.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS)).thenReturn(13).thenReturn(17);
        mockedRandomness.when(() -> Randomness.nextInt(3)).thenReturn(0).thenReturn(1).thenReturn(2);

        List<Integer> productions = List.of(0, 0, 0, 0);
        RefactorSequence parent = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        RefactorSequence mutant = parent.mutate();
        assertEquals(4, mutant.getProductions().size());
        assertEquals(0, mutant.getProductions().get(0));
        assertEquals(13, mutant.getProductions().get(1));
        assertEquals(0, mutant.getProductions().get(2));
        assertEquals(17, mutant.getProductions().get(3));
    }
}
