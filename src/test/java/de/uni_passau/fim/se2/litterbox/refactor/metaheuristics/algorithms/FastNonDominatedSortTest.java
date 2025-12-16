/*
 * Copyright (C) 2019-2025 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeDoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FastNonDominatedSortTest {

    @Test
    void fastNonDominatedSortCalculatesAndStoresFitnessValues() {
        MinimizingFitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);
        List<FitnessFunction<RefactorSequence>> fitnessFunctions = List.of(function1);

        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);

        List<RefactoringFinder> refactoringFinders = List.of(new MergeDoubleIfFinder());

        Program program = mock(Program.class);

        RefactorSequence solution = new RefactorSequence(program, mutation, crossover, new LinkedList<>(), refactoringFinders);
        when(function1.getFitness(solution)).thenReturn(1.0);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        fastNonDominatedSort.calculateFitnessValuesForSolutions(List.of(solution));
        assertEquals(1, solution.getFitness(function1));
    }

    @Test
    void testDominationCheck() {
        MaximizingFitnessFunction<RefactorSequence> function1 = mock(NumberOfHelloBlocks.class);
        MinimizingFitnessFunction<RefactorSequence> function2 = mock(NumberOfSmells.class);
        when(function2.isMinimizing()).thenReturn(true);
        List<FitnessFunction<RefactorSequence>> fitnessFunctions = List.of(function1, function2);

        // c3 dominates c2 and c1, c2 and c1 do not dominate each other
        RefactorSequence c1 = mock(RefactorSequence.class);
        RefactorSequence c2 = mock(RefactorSequence.class);
        RefactorSequence c3 = mock(RefactorSequence.class);

        when(c1.getFitness(function1)).thenReturn(0.6);
        when(c1.getFitness(function2)).thenReturn(0.8);

        when(c2.getFitness(function1)).thenReturn(0.7);
        when(c2.getFitness(function2)).thenReturn(0.9);

        when(c3.getFitness(function1)).thenReturn(0.7);
        when(c3.getFitness(function2)).thenReturn(0.8);

        Dominance<RefactorSequence> dominance = new Dominance<>(fitnessFunctions);
        assertFalse(dominance.test(c1, c2));
        assertFalse(dominance.test(c2, c1));

        assertTrue(dominance.test(c3, c1));
        assertTrue(dominance.test(c3, c2));

        assertFalse(dominance.test(c3, c3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFastNonDominatingSort() {
        RefactorSequence c1 = mock(RefactorSequence.class);
        RefactorSequence c2 = mock(RefactorSequence.class);
        RefactorSequence c3 = mock(RefactorSequence.class);
        RefactorSequence c4 = mock(RefactorSequence.class);
        List<RefactorSequence> solutions = Lists.newArrayList(c1, c2, c3, c4);

        Dominance<RefactorSequence> dominance = mock(Dominance.class);

        when(dominance.test(c1, c1)).thenReturn(false);
        when(dominance.test(c1, c2)).thenReturn(false);
        when(dominance.test(c1, c3)).thenReturn(false);
        when(dominance.test(c1, c4)).thenReturn(false);

        when(dominance.test(c2, c1)).thenReturn(false);
        when(dominance.test(c2, c2)).thenReturn(false);
        when(dominance.test(c2, c3)).thenReturn(false);
        when(dominance.test(c2, c4)).thenReturn(false);

        when(dominance.test(c3, c1)).thenReturn(true);
        when(dominance.test(c3, c2)).thenReturn(true);
        when(dominance.test(c3, c3)).thenReturn(false);
        when(dominance.test(c3, c4)).thenReturn(false);

        when(dominance.test(c4, c1)).thenReturn(true);
        when(dominance.test(c4, c2)).thenReturn(true);
        when(dominance.test(c4, c3)).thenReturn(false);
        when(dominance.test(c4, c4)).thenReturn(false);
        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(Collections.emptyList(), dominance);

        List<List<RefactorSequence>> fronts = fastNonDominatedSort.fastNonDominatedSort(solutions);
        assertEquals(2, fronts.size());
        assertEquals(2, fronts.getFirst().size());
        assertEquals(2, fronts.get(1).size());
        assertSame(c3, fronts.getFirst().getFirst());
        assertSame(c4, fronts.getFirst().get(1));
        assertSame(c1, fronts.get(1).getFirst());
        assertSame(c2, fronts.get(1).get(1));
    }
    
    @Test
    void testEmptyPopulation() {
        FastNonDominatedSort<RefactorSequence> sort = new FastNonDominatedSort<>(Collections.emptyList());
        List<List<RefactorSequence>> fronts = sort.fastNonDominatedSort(Collections.emptyList());
        assertTrue(fronts.isEmpty());
    }

    @Test
    void testSingleSolution() {
        RefactorSequence s1 = mock(RefactorSequence.class);
        FastNonDominatedSort<RefactorSequence> sort = new FastNonDominatedSort<>(Collections.emptyList());
        
        List<List<RefactorSequence>> fronts = sort.fastNonDominatedSort(List.of(s1));
        
        assertEquals(1, fronts.size());
        assertEquals(1, fronts.getFirst().size());
        assertSame(s1, fronts.getFirst().getFirst());
        verify(s1).setRank(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testChainDomination() {
        // S1 > S2 > S3
        RefactorSequence s1 = mock(RefactorSequence.class);
        RefactorSequence s2 = mock(RefactorSequence.class);
        RefactorSequence s3 = mock(RefactorSequence.class);
        List<RefactorSequence> solutions = List.of(s1, s2, s3);

        Dominance<RefactorSequence> dominance = mock(Dominance.class);
        
        // S1 dominates S2 and S3
        when(dominance.test(s1, s2)).thenReturn(true);
        when(dominance.test(s1, s3)).thenReturn(true);
        when(dominance.test(s1, s1)).thenReturn(false);

        // S2 dominates S3, dominated by S1
        when(dominance.test(s2, s3)).thenReturn(true);
        when(dominance.test(s2, s1)).thenReturn(false);
        when(dominance.test(s2, s2)).thenReturn(false);

        // S3 dominated by S1 and S2
        when(dominance.test(s3, s1)).thenReturn(false);
        when(dominance.test(s3, s2)).thenReturn(false);
        when(dominance.test(s3, s3)).thenReturn(false);

        FastNonDominatedSort<RefactorSequence> sort = new FastNonDominatedSort<>(Collections.emptyList(), dominance);
        List<List<RefactorSequence>> fronts = sort.fastNonDominatedSort(solutions);

        assertEquals(3, fronts.size());
        
        // Front 0: S1
        assertEquals(1, fronts.getFirst().size());
        assertSame(s1, fronts.getFirst().getFirst());
        verify(s1).setRank(0);

        // Front 1: S2
        assertEquals(1, fronts.get(1).size());
        assertSame(s2, fronts.get(1).getFirst());
        verify(s2).setRank(1);

        // Front 2: S3
        assertEquals(1, fronts.get(2).size());
        assertSame(s3, fronts.get(2).getFirst());
        verify(s3).setRank(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAllNonDominated() {
        // S1, S2, S3 - none dominate each other
        RefactorSequence s1 = mock(RefactorSequence.class);
        RefactorSequence s2 = mock(RefactorSequence.class);
        RefactorSequence s3 = mock(RefactorSequence.class);
        List<RefactorSequence> solutions = List.of(s1, s2, s3);

        Dominance<RefactorSequence> dominance = mock(Dominance.class);
        
        // No domination
        when(dominance.test(any(), any())).thenReturn(false);

        FastNonDominatedSort<RefactorSequence> sort = new FastNonDominatedSort<>(Collections.emptyList(), dominance);
        List<List<RefactorSequence>> fronts = sort.fastNonDominatedSort(solutions);

        assertEquals(1, fronts.size());
        assertEquals(3, fronts.getFirst().size());
        assertTrue(fronts.getFirst().contains(s1));
        assertTrue(fronts.getFirst().contains(s2));
        assertTrue(fronts.getFirst().contains(s3));
        
        verify(s1).setRank(0);
        verify(s2).setRank(0);
        verify(s3).setRank(0);
    }
}
