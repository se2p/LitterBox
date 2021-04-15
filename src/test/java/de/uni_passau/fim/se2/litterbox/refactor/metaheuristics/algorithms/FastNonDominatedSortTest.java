package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfSmells;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FastNonDominatedSortTest {

    FitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);
    FitnessFunction<RefactorSequence> function2 = mock(NumberOfSmells.class);
    List<FitnessFunction<RefactorSequence>> fitnessFunctions = List.of(function1, function2);

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

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        fastNonDominatedSort.calculateFitnessValuesForSolutions(List.of(solution));
        assertEquals(1, solution.getFitness(function1));
    }

    @Test
    void testDominationCheck() {
        // mock f1 maximizing
        when(function1.comparator()).thenReturn(Comparator.comparingDouble(c -> c.getFitness(function1)));
        // mock f2 minimizing
        when(function2.comparator()).thenReturn((c1, c2) -> Double.compare(c2.getFitness(function2), c1.getFitness(function2)));

        RefactorSequence c1 = mock(RefactorSequence.class);
        RefactorSequence c2 = mock(RefactorSequence.class);
        RefactorSequence c3 = mock(RefactorSequence.class);

        when(c1.getFitness(function1)).thenReturn(0.6);
        when(c1.getFitness(function2)).thenReturn(0.8);

        when(c2.getFitness(function1)).thenReturn(0.7);
        when(c2.getFitness(function2)).thenReturn(0.9);

        when(c3.getFitness(function1)).thenReturn(0.7);
        when(c3.getFitness(function2)).thenReturn(0.8);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        assertFalse(fastNonDominatedSort.dominates(c1, c2));
        assertFalse(fastNonDominatedSort.dominates(c2, c1));

        assertTrue(fastNonDominatedSort.dominates(c3, c1));
        assertTrue(fastNonDominatedSort.dominates(c3, c2));

        assertFalse(fastNonDominatedSort.dominates(c3, c3));
    }

    @Test
    void testFastNonDominatingSort() {
        RefactorSequence c1 = mock(RefactorSequence.class);
        RefactorSequence c2 = mock(RefactorSequence.class);
        RefactorSequence c3 = mock(RefactorSequence.class);
        RefactorSequence c4 = mock(RefactorSequence.class);
        List<RefactorSequence> solutions = Lists.newArrayList(c1, c2, c3, c4);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.dominates(c1, c1)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c1, c2)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c1, c3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c1, c4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(c2, c1)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c2, c2)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c2, c3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c2, c4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(c3, c1)).thenReturn(true);
        when(fastNonDominatedSort.dominates(c3, c2)).thenReturn(true);
        when(fastNonDominatedSort.dominates(c3, c3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c3, c4)).thenReturn(false);

        when(fastNonDominatedSort.dominates(c4, c1)).thenReturn(true);
        when(fastNonDominatedSort.dominates(c4, c2)).thenReturn(true);
        when(fastNonDominatedSort.dominates(c4, c3)).thenReturn(false);
        when(fastNonDominatedSort.dominates(c4, c4)).thenReturn(false);

        doNothing().when(fastNonDominatedSort).calculateFitnessValuesForSolutions(solutions);

        when(fastNonDominatedSort.fastNonDominatedSort(any())).thenCallRealMethod();

        List<List<RefactorSequence>> fronts = fastNonDominatedSort.fastNonDominatedSort(solutions);
        assertEquals(2, fronts.size());
        assertEquals(2, fronts.get(0).size());
        assertEquals(2, fronts.get(1).size());
        assertSame(c3, fronts.get(0).get(0));
        assertSame(c4, fronts.get(0).get(1));
        assertSame(c1, fronts.get(1).get(0));
        assertSame(c2, fronts.get(1).get(1));
    }
}
