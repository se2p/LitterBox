/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
        assertEquals(2, fronts.get(0).size());
        assertEquals(2, fronts.get(1).size());
        assertSame(c3, fronts.get(0).get(0));
        assertSame(c4, fronts.get(0).get(1));
        assertSame(c1, fronts.get(1).get(0));
        assertSame(c2, fronts.get(1).get(1));
    }
}
