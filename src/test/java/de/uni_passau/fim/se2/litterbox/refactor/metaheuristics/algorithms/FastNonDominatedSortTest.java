package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfSmells;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FastNonDominatedSortTest {

    @Test
    void fastNonDominatedSortCalculatesAndStoresFitnessValues() {
        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);

        List<RefactoringFinder> refactoringFinders = List.of(new DoubleIfFinder());

        Program program = mock(Program.class);
        when(program.deepCopy()).thenReturn(program);

        RefactorSequence solution = new RefactorSequence(mutation, crossover, new LinkedList<>(), refactoringFinders);
        MinimizingFitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);
        when(function1.getFitness(solution)).thenReturn(1.0);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(function1);
        fastNonDominatedSort.calculateFitnessValuesForSolutions(List.of(solution));
        assertEquals(1, solution.getFitness1());
    }

    @Test
    void testDominationCheck() {
        MinimizingFitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);

        RefactorSequence testSuite1 = mock(RefactorSequence.class);
        RefactorSequence testSuite2 = mock(RefactorSequence.class);
        RefactorSequence testSuite3 = mock(RefactorSequence.class);

        when(testSuite1.getFitness1()).thenReturn(0.6);
        when(testSuite2.getFitness1()).thenReturn(0.7);
        when(testSuite3.getFitness1()).thenReturn(0.7);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(function1);
        assertTrue(fastNonDominatedSort.dominates(testSuite1, testSuite2));
        assertFalse(fastNonDominatedSort.dominates(testSuite2, testSuite1));

        assertFalse(fastNonDominatedSort.dominates(testSuite3, testSuite1));
        assertFalse(fastNonDominatedSort.dominates(testSuite3, testSuite2));

        assertFalse(fastNonDominatedSort.dominates(testSuite3, testSuite3));
    }

    @Test
    void testFastNonDominatingSort() {
        RefactorSequence testSuite1 = mock(RefactorSequence.class);
        RefactorSequence testSuite2 = mock(RefactorSequence.class);
        RefactorSequence testSuite3 = mock(RefactorSequence.class);
        RefactorSequence testSuite4 = mock(RefactorSequence.class);
        List<RefactorSequence> solutions = Lists.newArrayList(testSuite1, testSuite2, testSuite3, testSuite4);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.dominates(testSuite1, testSuite1)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite1, testSuite2)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite1, testSuite3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite1, testSuite4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(testSuite2, testSuite1)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite2, testSuite2)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite2, testSuite3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite2, testSuite4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(testSuite3, testSuite1)).thenReturn(true);
        when(fastNonDominatedSort.dominates(testSuite3, testSuite2)).thenReturn(true);
        when(fastNonDominatedSort.dominates(testSuite3, testSuite3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite3, testSuite4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(testSuite4, testSuite1)).thenReturn(true);
        when(fastNonDominatedSort.dominates(testSuite4, testSuite2)).thenReturn(true);
        when(fastNonDominatedSort.dominates(testSuite4, testSuite3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(testSuite4, testSuite4)).thenReturn(false);

        doNothing().when(fastNonDominatedSort).calculateFitnessValuesForSolutions(solutions);

        when(fastNonDominatedSort.fastNonDominatedSort(any())).thenCallRealMethod();

        List<List<RefactorSequence>> fronts = fastNonDominatedSort.fastNonDominatedSort(solutions);
        assertEquals(2, fronts.size());
        assertEquals(2, fronts.get(0).size());
        assertEquals(2, fronts.get(1).size());
        assertSame(testSuite3, fronts.get(0).get(0));
        assertSame(testSuite4, fronts.get(0).get(1));
        assertSame(testSuite1, fronts.get(1).get(0));
        assertSame(testSuite2, fronts.get(1).get(1));
    }
}
