package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
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
import static org.mockito.Mockito.mock;

class RefactorSequenceCrossoverTest {
    MockedStatic<Randomness> mockedRandomness;
    Program program;
    Mutation<RefactorSequence> mutation;
    RefactorSequenceCrossover crossover;
    List<RefactoringFinder> refactoringFinders;

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");

        mockedRandomness = Mockito.mockStatic(Randomness.class);
        program = mock(Program.class);
        mutation = mock(RefactorSequenceMutation.class);
        crossover = new RefactorSequenceCrossover();
        refactoringFinders = List.of(new DoubleIfFinder());
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }


    @Test
    void testCrossover() {

        List<Integer> production1 = List.of(0, 0, 0);
        List<Integer> production2 = List.of(1, 1, 1);

        RefactorSequence parent1 = new RefactorSequence(program, mutation, crossover, production1, refactoringFinders);
        RefactorSequence parent2 = new RefactorSequence(program, mutation, crossover, production2, refactoringFinders);

        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(1);
        Pair<RefactorSequence> children = parent1.crossover(parent2);

        assertEquals(0, children.getFst().getProductions().get(0));
        assertEquals(0, children.getFst().getProductions().get(1));
        assertEquals(1, children.getFst().getProductions().get(2));

        assertEquals(1, children.getSnd().getProductions().get(0));
        assertEquals(1, children.getSnd().getProductions().get(1));
        assertEquals(0, children.getSnd().getProductions().get(2));
    }

    @Test
    void testCrossoverSmallProductions() {

        List<Integer> production1 = List.of(0, 0);
        List<Integer> production2 = List.of(1);

        RefactorSequence parent1 = new RefactorSequence(program, mutation, crossover, production1, refactoringFinders);
        RefactorSequence parent2 = new RefactorSequence(program, mutation, crossover, production2, refactoringFinders);

        mockedRandomness.when(() -> Randomness.nextInt(1)).thenReturn(0);
        Pair<RefactorSequence> children = parent1.crossover(parent2);

        assertEquals(0, children.getFst().getProductions().get(0));

        assertEquals(1, children.getSnd().getProductions().get(0));
        assertEquals(0, children.getSnd().getProductions().get(1));
    }
}
